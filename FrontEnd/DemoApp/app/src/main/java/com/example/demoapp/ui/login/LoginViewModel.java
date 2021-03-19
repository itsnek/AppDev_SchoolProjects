package com.example.demoapp.ui.login;

import androidx.annotation.WorkerThread;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import android.util.Patterns;

import com.example.demoapp.data.repository.UserRepository;
import com.example.demoapp.R;
import com.example.demoapp.data.model.User;
import com.example.demoapp.data.model.repository.RepositoryResponse;

public class LoginViewModel extends ViewModel
{

    private final MutableLiveData<LoginFormState> loginFormState;
    private final LiveData<LoginResult> loginResult;
    private final UserRepository userRepository;

    public LoginViewModel(UserRepository userRepository)
    {
        this.userRepository = userRepository;
        loginFormState = new MutableLiveData<>();

        loginResult = Transformations.map(userRepository.getResult(), new Function<RepositoryResponse<User>, LoginResult>()
        {
            @WorkerThread
            @Override
            public LoginResult apply(RepositoryResponse<User> input)
            {
                if (input.isSuccessful())
                {
                    return new LoginResult(new LoggedInUserView(input.getResponse().getUsername()));
                }
                else
                {
                    return new LoginResult(R.string.login_failed);
                }
            }
        });
    }

    LiveData<LoginFormState> getLoginFormState()
    {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult()
    {
        return loginResult;
    }

    public void login(String username, String password)
    {
        userRepository.login(username, password);
    }

    public void loginDataChanged(String username, String password)
    {
        if (!isUserNameValid(username))
        {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        }
        else if (!isPasswordValid(password))
        {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        }
        else
        {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    private boolean isUserNameValid(String username)
    {
        if (username == null)
        {
            return false;
        }

        return Patterns.EMAIL_ADDRESS.matcher(username).matches();

    }

    private boolean isPasswordValid(String password)
    {
        return password != null && password.trim().length() > 5;
    }
}