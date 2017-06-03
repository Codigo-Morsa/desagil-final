package pesadadobatata.songsync;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import butterknife.ButterKnife;
import butterknife.InjectView;
import android.support.annotation.NonNull;
import com.google.firebase.auth.AuthResult;

import java.util.HashMap;

import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity implements OnCompleteListener {
    private static final String TAG = "SignupActivity";

    @InjectView(R.id.input_name) EditText _nameText;
    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_signup) Button _signupButton;
    @InjectView(R.id.link_login) TextView _loginLink;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.inject(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        int bgid = R.drawable.bgtop;
      //  getActionBar().setBackgroundDrawable(getDrawable(bgid));

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.Theme_AppCompat_DayNight_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        final String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.d("kk", "onComplete: Failed=" + task.getException().getMessage());
                            onSignupFailed();
                        } else {
                            Toast.makeText(SignupActivity.this, "Usuário criado com sucesso",
                                    Toast.LENGTH_SHORT).show();
                            onSignupSuccess(task.getResult().getUser().getUid());
                            UserProfileChangeRequest addDisplayName = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            task.getResult().getUser().updateProfile(addDisplayName);
                        }
                    }
                });


//        new android.os.Handler().postDelayed(
//                new Runnable() {
//                    public void run() {
//                        // On complete call either onSignupSuccess or onSignupFailed
//                        // depending on success
//                        onSignupSuccess();
//                        // onSignupFailed();
//                    }
//                }, 6000);
    }

    public void onSignupSuccess(String uid) {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        writeNewUser(uid, _nameText.getText().toString());
        Log.d("NEWLY CREATED USER:", String.valueOf(uid));
        finish();
    }

    private void writeNewUser(String userId, String name) {
        HashMap<String, String> user = new HashMap<>();
        user.put("username", name);
        HashMap<String, String> username = new HashMap<>();
        username.put("uid", userId);
        mDatabase.child("users").child(userId).setValue(user);
        mDatabase.child("usernames").child(name.toLowerCase()).setValue(username);
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Falha ao criar usuário, esse email ja pode estar sendo usado", Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }


    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 5) {
            _nameText.setError("Pelo menos 5 caracteres");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        String upper = name.toUpperCase();
        for (int i = 0; i< name.length() ; i++){
            if (!Character.isDigit(name.charAt(i)) && name.charAt(i) == upper.charAt(i)){
                _nameText.setError("Nome não pode possuir letras maiúsculas");
                valid = false;
            }
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Digite um email válido");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6 || password.length() > 25) {
            _passwordText.setError("Deve possuir de 8 a 25 caracteres");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    @Override
    public void onComplete(@NonNull Task task) {

    }
}
