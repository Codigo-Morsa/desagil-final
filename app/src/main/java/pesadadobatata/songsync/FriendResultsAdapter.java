package pesadadobatata.songsync;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import pesadadobatata.songsync.R;
import samplesearch.SearchResultsAdapter;

public class FriendResultsAdapter extends RecyclerView.Adapter<FriendResultsAdapter.ViewHolder> {

    private final List<Friend> mItems = new ArrayList<>();
    private final Context mContext;
    private final ItemSelectedListener mListener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView title;
        public final ImageView image;
        public final TextView subtitle;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.entity_title);
            subtitle = (TextView) itemView.findViewById(R.id.entity_subtitle);
            image = (ImageView) itemView.findViewById(R.id.entity_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            notifyItemChanged(getLayoutPosition());
            mListener.onItemSelected(v, mItems.get(getAdapterPosition()));
        }
    }

    public interface ItemSelectedListener {
        void onItemSelected(View itemView, Friend item);
    }

    public FriendResultsAdapter(Context context, ItemSelectedListener listener) {
        mContext = context;
        mListener = listener;
    }

    public void clearData() {
        mItems.clear();
    }

    public void addData(List<Friend> items) {
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Friend item = mItems.get(position);

        holder.title.setText(item.getUserName());

        List<String> names = new ArrayList<>();
//        for (ArtistSimple i : item) {
//            names.add(i.name);
//        }
        Joiner joiner = Joiner.on(", ");
        holder.subtitle.setText(joiner.join(names));

//        Image image = item.album.images.get(0);
//        if (image != null) {
//            Picasso.with(mContext).load(image.url).into(holder.image);
//        }
    }



    @Override
    public int getItemCount() {
        return mItems.size();
    }
}