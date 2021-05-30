package com.example.demoapp.data.repository;

import android.provider.ContactsContract;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.demoapp.data.Event;
import com.example.demoapp.data.datasource.ApiDataSource;
import com.example.demoapp.data.model.Activity;
import com.example.demoapp.data.model.Item;
import com.example.demoapp.data.model.Place;
import com.example.demoapp.data.model.Post;
import com.example.demoapp.data.model.api.request.SearchQueryModel;
import com.example.demoapp.data.model.datasource.DataSourceResponse;
import com.example.demoapp.data.model.repository.RepositoryResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContentRepository extends Repository
{
    private static volatile ContentRepository instance;
    private final ApiDataSource dataSource;

    private final MediatorLiveData<RepositoryResponse<List<Item>>> searchResult;
    private final MediatorLiveData<RepositoryResponse<List<Item>>> feedResult;
    private final MediatorLiveData<RepositoryResponse<Event<Boolean>>> uploadResult;
    private final MediatorLiveData<RepositoryResponse<Post>> postResult;
    private final MediatorLiveData<RepositoryResponse<List<String>>> citiesData;
    private final MediatorLiveData<RepositoryResponse<List<Activity>>> activitiesResult;
    private final MediatorLiveData<RepositoryResponse<Event<Place>>> placeResult;

    public MediatorLiveData<RepositoryResponse<List<Activity>>> getNearActivitiesResult()
    {
        return nearActivitiesResult;
    }

    private final MediatorLiveData<RepositoryResponse<List<Activity>>> nearActivitiesResult;

    private final MutableLiveData<Activity> currentActivity;
    private final MutableLiveData<Post> currentPost;

    private ContentRepository(ApiDataSource dataSource)
    {
        this.dataSource = dataSource;

        searchResult = new MediatorLiveData<>();
        feedResult = new MediatorLiveData<>();
        uploadResult = new MediatorLiveData<>();
        postResult = new MediatorLiveData<>();
        citiesData = new MediatorLiveData<>();
        activitiesResult = new MediatorLiveData<>();
        nearActivitiesResult = new MediatorLiveData<>();
        placeResult = new MediatorLiveData<>();

        currentActivity = new MutableLiveData<>();
        currentPost = new MutableLiveData<>();
    }

    public static ContentRepository getInstance(ApiDataSource dataSource)
    {
        if (instance == null)
        {
            instance = new ContentRepository(dataSource);
        }
        return instance;
    }

    public void searchNearActivities(double latitude, double longtitude, double radius)
    {
        LiveData<DataSourceResponse<List<Activity>>> result = dataSource.searchNearActivities(latitude, longtitude, radius, loadFromPrefs("JWToken"));
        nearActivitiesResult.addSource(result, new Observer<DataSourceResponse<List<Activity>>>()
        {
            @Override
            public void onChanged(DataSourceResponse<List<Activity>> response)
            {
                if (response.isSuccessful())
                {
                    nearActivitiesResult.setValue(new RepositoryResponse<>(response.getResponse()));
                }
                else
                {
                    nearActivitiesResult.setValue(new RepositoryResponse<>(response.getErrorMessage()));
                }

                nearActivitiesResult.removeSource(result);
            }
        });
    }

    public void search(String query, String country, String city, String type, int radius)
    {
        LiveData<DataSourceResponse<List<Item>>> dataSourceResult;

        if (type.equalsIgnoreCase("activity"))
        {
            dataSourceResult= dataSource.searchActivities(new SearchQueryModel(query, country, city, radius), loadFromPrefs("JWToken"));
        }
        else if (type.equalsIgnoreCase("post"))
        {
            dataSourceResult= dataSource.searchPosts(new SearchQueryModel(query, country, city, radius), loadFromPrefs("JWToken"));
        }
        else
        {
            dataSourceResult = dataSource.searchUsers(query, loadFromPrefs("JWToken"));
        }

        searchResult.addSource(dataSourceResult, new Observer<DataSourceResponse<List<Item>>>()
        {
            @Override
            public void onChanged(@Nullable DataSourceResponse<List<Item>> result)
            {
                if (result.isSuccessful())
                {
                    searchResult.setValue(new RepositoryResponse<>(result.getResponse()));
                }
                else
                {
                    searchResult.setValue(new RepositoryResponse<>(result.getErrorMessage()));
                }

                searchResult.removeSource(dataSourceResult);
            }
        });
    }

    public void updateFeed(boolean self)
    {
        LiveData<DataSourceResponse<List<Item>>> dataSourceResult = dataSource.getFeed(self, loadFromPrefs("JWToken"));
        feedResult.addSource(dataSourceResult, result ->
        {
            if (!result.isSuccessful())
            {
                feedResult.setValue(new RepositoryResponse<>(result.getErrorMessage()));
            }
            else
            {
                feedResult.setValue(new RepositoryResponse<>(result.getResponse()));
            }

            feedResult.removeSource(dataSourceResult);
        });
    }

    public LiveData<RepositoryResponse<List<Item>>> getSearchResult()
    {
        return searchResult;
    }

    public LiveData<RepositoryResponse<List<Item>>> getFeedResult()
    {
        return feedResult;
    }

    public LiveData<RepositoryResponse<Event<Boolean>>> getUploadResult()
    {
        return uploadResult;
    }

    public void uploadPost(String title, String description, Date date)
    {
        Post post = currentPost.getValue();
        post.setDate(date);
        post.setDescription(description);
        post.setTitle(title);

        LiveData<DataSourceResponse<Boolean>> result = dataSource.uploadPost(post, loadFromPrefs("JWToken"));
        uploadResult.addSource(result, new Observer<DataSourceResponse<Boolean>>()
        {
            @Override
            public void onChanged(DataSourceResponse<Boolean> booleanDataSourceResponse)
            {
                uploadResult.setValue(new RepositoryResponse<>(new Event<>(booleanDataSourceResponse.getResponse())));
                uploadResult.removeSource(result);
            }
        });
    }

    public void addImages(List<String> images)
    {
        Post post = currentPost.getValue();
        post.getImages().addAll(images);
        currentPost.setValue(post);
    }

    public void storeActivity(Activity activity)
    {
        currentActivity.setValue(activity);
    }

    public void addActivity(Activity activity)
    {
        currentPost.getValue().getActivities().addLast(activity);

        currentPost.setValue(currentPost.getValue());
    }

    public void initializePostData()
    {
        if (currentPost.getValue() == null)
        {
            currentPost.setValue(new Post());
        }
    }

    public void loadPost(int postID)
    {
        LiveData<DataSourceResponse<Post>> result = dataSource.getPost(postID, loadFromPrefs("JWToken"));
        postResult.addSource(result, new Observer<DataSourceResponse<Post>>()
        {
            @Override
            public void onChanged(DataSourceResponse<Post> postDataSourceResponse)
            {
                if (postDataSourceResponse.isSuccessful())
                {
                    postResult.setValue(new RepositoryResponse<>(postDataSourceResponse.getResponse()));
                }
                else
                {
                    postResult.setValue(new RepositoryResponse<>(postDataSourceResponse.getErrorMessage()));
                }

                postResult.removeSource(result);
            }
        });
    }

    public void getCities(String country)
    {
        LiveData<DataSourceResponse<List<String>>> result = dataSource.getCities(country);
        citiesData.addSource(result, new Observer<DataSourceResponse<List<String>>>()
        {
            @Override
            public void onChanged(DataSourceResponse<List<String>> response)
            {
                if (response.isSuccessful())
                {
                    citiesData.setValue(new RepositoryResponse<>(response.getResponse()));
                }
                else
                {
                    citiesData.setValue(new RepositoryResponse<>(response.getErrorMessage()));
                }

                citiesData.removeSource(result);
            }
        });
    }

    public void getPostActivities(int postID)
    {
        LiveData<DataSourceResponse<List<Activity>>> result = dataSource.getPostActivities(postID, loadFromPrefs("JWToken"));
        activitiesResult.addSource(result, new Observer<DataSourceResponse<List<Activity>>>()
        {
            @Override
            public void onChanged(DataSourceResponse<List<Activity>> response)
            {
                if (response.isSuccessful())
                {
                    activitiesResult.setValue(new RepositoryResponse<>(response.getResponse()));
                }
                else
                {
                    activitiesResult.setValue(new RepositoryResponse<>(response.getErrorMessage()));
                }

                activitiesResult.removeSource(result);
            }
        });
    }

    public void getLocationInfo(double latitude, double longitude)
    {
        LiveData<DataSourceResponse<Place>> result = dataSource.getLocationInfo(latitude, longitude);
        placeResult.addSource(result, response ->
        {
            if (response.isSuccessful())
            {
                placeResult.setValue(new RepositoryResponse<>(new Event<>(response.getResponse())));
            }
            else
            {
                activitiesResult.setValue(new RepositoryResponse<>(response.getErrorMessage()));
            }

            placeResult.removeSource(result);
        });
    }

    public void getLocationInfo(String query)
    {
        LiveData<DataSourceResponse<Place>> result = dataSource.getCoordinates(query);
        placeResult.addSource(result, response ->
        {
            if (response.isSuccessful())
            {
                placeResult.setValue(new RepositoryResponse<>(new Event<>(response.getResponse())));
            }
            else
            {
                activitiesResult.setValue(new RepositoryResponse<>(response.getErrorMessage()));
            }

            placeResult.removeSource(result);
        });
    }

    public void resetPostData()
    {
        currentPost.setValue(null);
    }

    public MutableLiveData<Activity> getCurrentActivity()
    {
        return currentActivity;
    }

    public MutableLiveData<Post> getCurrentPost()
    {
        return currentPost;
    }

    public LiveData<RepositoryResponse<Post>> getPostResult()
    {
        return postResult;
    }

    public LiveData<RepositoryResponse<List<String>>> getCitiesResult()
    {
        return citiesData;
    }

    public MediatorLiveData<RepositoryResponse<List<Activity>>> getActivitiesResult()
    {
        return activitiesResult;
    }

    public MediatorLiveData<RepositoryResponse<Event<Place>>> getPlaceResult()
    {
        return placeResult;
    }
}
