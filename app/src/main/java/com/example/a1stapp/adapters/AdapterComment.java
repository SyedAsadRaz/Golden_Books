package com.example.a1stapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.a1stapp.MyApplication;
import com.example.a1stapp.R;
import com.example.a1stapp.databinding.RowCommentBinding;
import com.example.a1stapp.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.HolderComment>{

    private Context context;
    private ArrayList<ModelComment> commentArrayList;

    private FirebaseAuth firebaseAuth;

    //view binding
    private RowCommentBinding binding;

    //Constructor
    public AdapterComment(Context context, ArrayList<ModelComment> commentArrayList) {
        this.context = context;
        this.commentArrayList = commentArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate bind the view xml
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent,false);

        return new HolderComment(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderComment holder, int position) {
        //get datat from specfic position of list set data handl cliks etc

        //get data
        ModelComment modelComment = commentArrayList.get(position);
        String id = modelComment.getId();
        String bookId = modelComment.getBookId();
        String comment = modelComment.getComment();
        String uid = modelComment.getUid();
        String timestamp = modelComment.getTimestamp();

        //from data already mad =e function in Myapplictains class
        String data = MyApplication.formatTimestamp(timestamp);

        //set data
        holder.dateTv.setText(data);
        holder.commentTv.setText(comment);
//        holder.nameTv.setText();
        //we dont have user name and pic so we will load it using uid we stored in stored in each comment
        loadUserDetails(modelComment, holder);

        //handle click show option to delete comment
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //requirments to delete a comment
//                user must be logged in
                //uid comment must be same as uid of lod=gged in user
                if (firebaseAuth.getCurrentUser() != null && uid.equals(firebaseAuth.getUid())){
                    deleteComment(modelComment,holder);
                }


            }
        });
    }

    private void deleteComment(ModelComment modelComment, HolderComment holder) {
        //show confirm dialog before deleting comment
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Comment")
                .setMessage("Are your sure you want to delete this comment?")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //delete from dialog cliked begin dlete

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(modelComment.getBookId())
                                .child("Comments")
                                .child(modelComment.getId())
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Comment Deleted!", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Field to dlete becuase: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).show();
    }

    private void loadUserDetails(ModelComment modelComment, HolderComment holder) {
        String uid = modelComment.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get
                        String name = ""+snapshot.child("Name").getValue();
                        String profileImage = ""+snapshot.child("ProileImage").getValue();
                        //set
                        holder.nameTv.setText(name);
                        try{
                            Glide.with(context)
                                    .load(profileImage)
                                    .placeholder(R.drawable.ic_baseline_person_24)
                                    .into(holder.profileIv);

                        }catch (Exception e){
                            holder.profileIv.setImageResource(R.drawable.ic_baseline_person_24);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return commentArrayList.size();
    }

    //View holder class for row_commentxml
    class HolderComment extends RecyclerView.ViewHolder{

        //ui views of ro wcommentxml
        TextView nameTv,dateTv,commentTv;
        ShapeableImageView profileIv;

        public HolderComment(@NonNull View itemView) {
            super(itemView);

            //init ui views
            profileIv = binding.profileIv;
            nameTv = binding.nameTv;
            dateTv = binding.dateTv;
            commentTv = binding.commentTv;
        }
    }
}
