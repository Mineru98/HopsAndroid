package com.mineru.hops.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionMenu;
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
import com.mineru.hops.CardDialog;
import com.mineru.hops.UserManage.Model.ImageDTO;
import com.mineru.hops.Function.MakeCard.MakeCard1;
import com.mineru.hops.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Mineru on 2017-09-08.
 */

public class Home extends Fragment {
    private static final String TAG ="HomeFragment";
    private CardDialog mCardDialog;
    private RecyclerView recyclerView;

    private List<ImageDTO> imageDTOs = new ArrayList<>();
    private List<String> uidLists = new ArrayList<>();
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private FloatingActionMenu fam;
    private com.github.clans.fab.FloatingActionButton fabAdd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment_layout,container,false);
        setHasOptionsMenu(true);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        recyclerView = (RecyclerView) view.findViewById(R.id.home_fragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        final BoardRecyclerViewAdapter boardRecyclerViewAdapter = new BoardRecyclerViewAdapter();
        recyclerView.setAdapter(boardRecyclerViewAdapter);

        fabAdd = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.fab_add);
        fam = (FloatingActionMenu) view.findViewById(R.id.fab_plus_home);

        fabAdd.setOnClickListener(onButtonClick());
        fam.setClosedOnTouchOutside(true);

        fam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fam.isOpened()) {
                    fam.close(true);
                }
            }
        });
        database.getReference().child("Users/"+auth.getCurrentUser().getUid()+"/"+"Card/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                imageDTOs.clear();
                uidLists.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ImageDTO imageDTO = snapshot.getValue(ImageDTO.class);
                    String uidKey = snapshot.getKey();
                    imageDTOs.add(imageDTO);
                    uidLists.add(uidKey);
                }
                boardRecyclerViewAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    private View.OnClickListener onButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == fabAdd) {
                    Intent intent = new Intent(getActivity(), MakeCard1.class);
                    startActivity(intent);
                    fam.close(true);
                }
            }
        };
    }

    class BoardRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board, parent, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ((CustomViewHolder)holder).textView.setText(imageDTOs.get(position).inputName);
            ((CustomViewHolder)holder).textView2.setText(imageDTOs.get(position).inputCompany);
            ((CustomViewHolder)holder).textView3.setText(imageDTOs.get(position).inputPosition);

            ((CustomViewHolder) holder).btnFront.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v){
                    mCardDialog = new CardDialog(getActivity(),imageDTOs.get(position).imageUrl,imageDTOs.get(position).inputName, imageDTOs.get(position).inputCompany,
                            imageDTOs.get(position).inputPosition);
                    mCardDialog.show();
                    return false;
                }
            });
            ((CustomViewHolder)holder).mCallbtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    startActivity(new Intent("android.intent.action.DIAL", Uri.parse("tel:"+imageDTOs.get(position).inputPhoneNumber)));
                }
            });

            ((CustomViewHolder)holder).mMailbtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                    Uri uri = Uri.parse("smsto:"+imageDTOs.get(position).inputPhoneNumber);
                    Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                    it.putExtra("sms_body", "Hops Message Test");
                    startActivity(it);
                }
            });
            Glide.with(holder.itemView.getContext()).load(imageDTOs.get(position).imageUrl).into(((CustomViewHolder)holder).imageView);
            ((CustomViewHolder)holder).pinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStarClicked(database.getReference().child("Users/"+auth.getCurrentUser().getUid()+"/"+"Card").child(uidLists.get(position)));
                }
            });

            if (imageDTOs.get(position).cardpins.containsKey(auth.getCurrentUser().getUid())) {
                ((CustomViewHolder)holder).pinButton.setImageResource(R.drawable.icon_cardpin_on);

            }else {
                ((CustomViewHolder)holder).pinButton.setImageResource(R.drawable.icon_cardpin_off);
            }
            ((CustomViewHolder)holder).deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    delete_content(position);
                }
            });
        }
        @Override
        public int getItemCount() {
            return imageDTOs.size();
        }

        private void onStarClicked(DatabaseReference postRef) {
            postRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    ImageDTO imageDTO = mutableData.getValue(ImageDTO.class);
                    if (imageDTO == null) {
                        return Transaction.success(mutableData);
                    }

                    if (imageDTO.cardpins.containsKey(auth.getCurrentUser().getUid())) {
                        // Unstar the post and remove self from stars
                        imageDTO.cardpinCount = imageDTO.cardpinCount - 1;
                        imageDTO.cardpins.remove(auth.getCurrentUser().getUid());
                    } else {
                        // Star the post and add self to stars
                        imageDTO.cardpinCount = imageDTO.cardpinCount + 1;
                        imageDTO.cardpins.put(auth.getCurrentUser().getUid(), true);
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
        private void delete_content(final int position){

            storage.getReference().child("Users/"+auth.getCurrentUser().getUid()).child(imageDTOs.get(position).imageName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    database.getReference().child("Users/"+auth.getCurrentUser().getUid()+"/"+"Card/").child(uidLists.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(getActivity(), "Delete Success", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "Delete Fail", Toast.LENGTH_SHORT).show();

                }
            });

        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ImageView pinButton;
            ImageView deleteButton;
            TextView textView;
            TextView textView2;
            TextView textView3;

            RelativeLayout btnFront;
            ImageButton mCallbtn;
            ImageButton mMailbtn;


            public CustomViewHolder(View view) {
                super(view);
                btnFront = (RelativeLayout) view.findViewById(R.id.cardLayer);
                mCallbtn = (ImageButton) view.findViewById(R.id.imgBtn1);
                mMailbtn = (ImageButton) view.findViewById(R.id.imgBtn2);


                imageView = (ImageView) view.findViewById(R.id.item_imageView);
                textView = (TextView) view.findViewById(R.id.item_textView);
                textView2 = (TextView) view.findViewById(R.id.item_textView2);
                textView3 = (TextView) view.findViewById(R.id.item_textView3);
                pinButton = (ImageView) view.findViewById(R.id.card_pin);
                deleteButton = (ImageView) view.findViewById(R.id.card_delete);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
    }
}