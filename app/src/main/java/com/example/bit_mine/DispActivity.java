package com.example.bit_mine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.OnSuccessListener;
        import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
        import com.google.firebase.storage.FirebaseStorage;
        import com.google.firebase.storage.ListResult;
        import com.google.firebase.storage.StorageReference;

        import java.util.ArrayList;
import java.util.List;

public class DispActivity extends AppCompatActivity {
    ArrayList<String> imagelist;
    RecyclerView recyclerView;
    StorageReference root;
    ProgressBar progressBar;
    ImageAdapter adapter;
    FirebaseAuth mAuth;
    Spinner spinner;
    ListView listView;
    FirebaseFirestore db;
    FirebaseFirestore fstore ;
    String mailPath=null;
    View v;
    ArrayList<String> fol = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disp);
        imagelist=new ArrayList<>();
        recyclerView=findViewById(R.id.recyclerview);
        adapter=new ImageAdapter(imagelist,this);
        progressBar=findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);
        mAuth = FirebaseAuth.getInstance();


        StorageReference listRef = FirebaseStorage.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString());
        //recyclerView.setLayoutManager(new LinearLayoutManager(null));

        GridLayoutManager layoutManager=new GridLayoutManager(this,2);

        // at last set adapter to recycler view.
        recyclerView.setLayoutManager(layoutManager);

        listView = findViewById(R.id.list_item);






        listRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for(StorageReference file:listResult.getItems()){
                    file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imagelist.add(uri.toString());
                           // Log.e("Itemvalue",uri.toString());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            recyclerView.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    /***************************** drop down for path selection ********************************/
    public void showAlertDialogButtonClicked(View view)
    {

        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        builder.setTitle("Super User Settings");
        // set the custom layout


        final View customLayout
                = getLayoutInflater()
                .inflate(
                        R.layout.dialog_custom_path_selector,
                        null);
        builder.setView(customLayout);

        // add a button
        builder
                .setPositiveButton(
                        "OKAY!",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which)
                            {

                                // send data from the
                                // AlertDialog to the Activity
                            }
                        })

                .setNegativeButton(
                        "Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }
                );

        // create and show
        // the alert dialog
        AlertDialog dialog
                = builder.create();
        dialog.show();
    }

}
