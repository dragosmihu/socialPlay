package com.example.socialplay.Adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialplay.Model.Post;
import com.example.socialplay.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> implements Filterable {
        private List<Post> posts;
        private List<Post> postsListFull;

         class PostViewHolder extends RecyclerView.ViewHolder {
             TextView fullName, date, time, description;
             CircleImageView profilePicture;
             ImageView postPicture;
             Button ShareButton;
             public PostViewHolder(@NonNull View itemView)
             {
                 super(itemView);
                 fullName = itemView.findViewById(R.id.post_user_name);
                 date = itemView.findViewById(R.id.post_date);
                 time = itemView.findViewById(R.id.post_time);
                 description = itemView.findViewById(R.id.post_description);
                 profilePicture = itemView.findViewById(R.id.post_profile_picture);
                 postPicture = itemView.findViewById(R.id.post_picture);
                 ShareButton = itemView.findViewById(R.id.share);
             }

             public void SetParams( Post post) {
                 this.fullName.setText(post.fullName);
                 this.date.setText("   " + post.date);
                 this.time.setText("   " + post.time);
                 this.description.setText(post.description);
                 Picasso.get().load(post.profilePicture).into(this.profilePicture);
                 Picasso.get().load(post.postPicture).into(this.postPicture);
             }
         }

        public PostAdapter(List<Post> posts)
        {
            this.posts = posts;
            postsListFull = new ArrayList<>(posts);
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
            return new PostViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
            Post currentItem = posts.get(position);
            holder.SetParams(currentItem);
            holder.ShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent sharing = new Intent();
                    Uri bmpuri = getLocalBitmapUri(holder.postPicture);
                    sharing.setAction(Intent.ACTION_SEND);
                    sharing.putExtra(Intent.EXTRA_STREAM, bmpuri);
                    sharing.setType("image/jpeg");
                    view.getContext().startActivity(Intent.createChooser(sharing, null));
                }
            });
        }
        public Uri getLocalBitmapUri(ImageView imageView) {
            // Extract Bitmap from ImageView drawable
            Drawable drawable = imageView.getDrawable();
            Bitmap bmp = null;
            if (drawable instanceof BitmapDrawable){
                bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            } else {
                return null;
            }
            // Store image to default external storage directory
            Uri bmpUri = null;
            try {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                File file =  new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
                file.getParentFile().mkdirs();
                FileOutputStream out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.close();
                bmpUri = Uri.fromFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmpUri;
        }
        @Override
        public Filter getFilter() {
            return filter;
        }


        @Override
        public int getItemCount() {
            return posts.size();
        }
        private Filter filter= new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Post> filterList = new ArrayList<>();
            if(constraint == null || constraint.length()==0){
                filterList.addAll(postsListFull);
            }
            else{
                String pattern = constraint.toString().toLowerCase().trim();
                for(Post item :postsListFull){
                    if(item.getFullName().toLowerCase().contains(pattern)){
                        filterList.add(item);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filterList;
            return filterResults;

        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            posts.clear();
            posts.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };
}
