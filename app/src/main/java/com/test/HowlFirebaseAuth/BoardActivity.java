package com.test.HowlFirebaseAuth;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BoardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private List<ImageDTO> imageDTOList = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage mFirebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final BoardRecyclerViewAdapter boardRecyclerViewAdapter = new BoardRecyclerViewAdapter();
        recyclerView.setAdapter(boardRecyclerViewAdapter);

        //(옵저버 패턴) 글자 하나씩 바뀔 때 마다 자동갱신
        //관찰자가 있는 동안 계속 갱신됌
        //AllList
        mFirebaseDatabase.getReference().child("images").addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                imageDTOList.clear();
                uidList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ImageDTO imageDTO = snapshot.getValue(ImageDTO.class);
                    String uidKey = snapshot.getKey();
                    imageDTOList.add(imageDTO);
                    uidList.add(uidKey);
                }
                //역순 정렬
                Collections.reverse(imageDTOList);
                Collections.reverse(uidList);
                //Refresh
                boardRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //뷰 아답터 item_board.xml 참조
    class BoardRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board, parent, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            //Title, Description
            ((CustomViewHolder)holder).textView.setText(imageDTOList.get(position).getTitle());
            ((CustomViewHolder)holder).textView2.setText(imageDTOList.get(position).getDescription());

            //Image
            Glide.with(holder.itemView.getContext())
                    .load(imageDTOList.get(position).getImageUrl())
                    .into(((CustomViewHolder) holder).imageView);

            //starButton
            ((CustomViewHolder)holder).starButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    onStarClicked(mFirebaseDatabase.getReference().child("images").child(uidList.get(position) ) );
                }
            });

            //starButton Map<String, True>
            if(imageDTOList.get(position).getStars().containsKey(mFirebaseAuth.getCurrentUser().getUid())){
                ((CustomViewHolder)holder).starButton.setImageResource(R.drawable.ic_favorite_black_24dp);
            }else{
                ((CustomViewHolder)holder).starButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
            }

            ((CustomViewHolder)holder).deleteButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    delete_content(position);
                }
            });
        }

        public int getItemCount(){
            return imageDTOList.size();
        }

        private void onStarClicked(DatabaseReference postRef) {
            postRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    ImageDTO imageDTO = mutableData.getValue(ImageDTO.class);
                    if (imageDTO == null) {
                        return Transaction.success(mutableData);
                    }

                    if (imageDTO.getStars().containsKey(mFirebaseAuth.getCurrentUser().getUid())) {
                        // Unstar the post and remove self from stars
                        imageDTO.setStartCount(imageDTO.getStartCount() - 1);
                        imageDTO.getStars().remove(mFirebaseAuth.getCurrentUser().getUid());
                    } else {
                        // Star the post and add self to stars
                        imageDTO.setStartCount(imageDTO.getStartCount() + 1);
                        imageDTO.getStars().put(mFirebaseAuth.getCurrentUser().getUid(), true);
                    }

                    // Set value and report transaction success
                    mutableData.setValue(imageDTO);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b,
                                       DataSnapshot dataSnapshot) {
                    // Transaction completed

                }
            });
        }

        //Storage Image Delete
        private void delete_content(final int position){
            mFirebaseStorage.getReference().child("images").child(imageDTOList.get(position).getImageName()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    mFirebaseDatabase.getReference()
                            .child("images")
                            .child(uidList.get(position))//DB Key Value
                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(BoardActivity.this, "Remove Successfully", Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                    Toast.makeText(BoardActivity.this, "Remove Successfully", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(BoardActivity.this, "Remove Failed", Toast.LENGTH_LONG).show();
                }
            });


        }

        private class CustomViewHolder extends RecyclerView.ViewHolder{
            ImageView imageView;
            TextView textView;
            TextView textView2;
            ImageView starButton;
            ImageView deleteButton;

            public CustomViewHolder(View view){
                super(view);
                imageView = (ImageView) view.findViewById(R.id.item_imageView);
                textView = (TextView) view.findViewById(R.id.item_textView);
                textView2 = (TextView) view.findViewById(R.id.item_textView2);
                starButton = (ImageView) view.findViewById(R.id.item_starButton_imageView);
                deleteButton = (ImageView) view.findViewById(R.id.item_delete_imageView);
            }
        }
    }
}
