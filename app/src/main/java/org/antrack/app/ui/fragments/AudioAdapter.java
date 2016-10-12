package org.antrack.app.ui.fragments;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.antrack.app.service.U;
import org.antrack.app.ui.RecyclerViewAnim;

import java.util.List;
import java.util.concurrent.TimeUnit;

import app.R;

class AudioAdapter extends RecyclerViewAnim.Adapter<AudioAdapter.ModuleViewHolder> {
    static class ModuleViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView length;
        TextView date;

        ModuleViewHolder(View view) {
            super(view);
            cv = (CardView) view.findViewById(R.id.cardview_audios);
            date = (TextView) view.findViewById(R.id.cardview_audios_date);
            length = (TextView) view.findViewById(R.id.cardview_audios_length);

        }
    }

    private List<Audio> audios;
    private Activity activity;

    AudioAdapter(Activity activity, List<Audio> audios){
        this.activity = activity;
        this.audios = audios;
    }

    public void update(List<Audio> audios) {
        this.audios = audios;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ModuleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_audios, viewGroup, false);
        return new ModuleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ModuleViewHolder viewHolder, final int i) {
        String a[] = audios.get(i).file.substring(0, audios.get(i).file.lastIndexOf('.')).split("-");
        final String date = a[0]+"."+a[1]+"."+a[2]+" "+a[3]+":"+a[4]+":"+a[5];

        viewHolder.date.setText(date);
        viewHolder.length.setText(String.format("%02d:%02d",
                TimeUnit.SECONDS.toMinutes(audios.get(i).length),
                TimeUnit.SECONDS.toSeconds(audios.get(i).length) %
                        TimeUnit.MINUTES.toSeconds(1))
        );

        viewHolder.cv.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AudioPlayDialog aplayDialog = new AudioPlayDialog(activity);
                aplayDialog.show(date, U.getFullPath(AudioFragment.audioDir + audios.get(i).file));
            }
        });
    }

    @Override
    public int getItemCount() {
        return audios.size();
    }

}
