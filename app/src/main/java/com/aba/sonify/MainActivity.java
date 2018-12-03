package com.aba.sonify;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 999;
    private MediaPlayer mMediaPlayer;
    private String[] mAudioPath;
    private int currentIndex;
    private ListView mListView;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;
    private ImageButton mPauseButton;
    private ImageButton mPreviousButton;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMediaPlayer = new MediaPlayer();
        mListView = (ListView) findViewById(R.id.listView1);
        mPlayButton = (ImageButton) findViewById(R.id.playButton);
        mNextButton = (ImageButton) findViewById(R.id.nextButton);
        mPauseButton = (ImageButton) findViewById(R.id.pauseButton);
        mPreviousButton = (ImageButton) findViewById(R.id.previousButton);

        init();
    }

    private void init() {
        final String[] mMusicList = getAudioList();

        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, mMusicList) {
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                CheckedTextView ctv = (CheckedTextView) view.findViewById(android.R.id.text1);
                ctv.setFocusable(false);
                ctv.setText(mMusicList[position]);
                ctv.setChecked(position == currentIndex);
                return view;
            }
        };
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                try {
                    currentIndex = arg2;
                    ((ArrayAdapter) mListView.getAdapter()).notifyDataSetChanged();
                    playSong(mAudioPath[currentIndex]);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSong();
            }
        });
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseSong();
            }
        });
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextSong();
            }
        });
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousSong();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                // User refused to grant permission.
            }
        }
    }

    private String[] getAudioList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
            return new String[0];
        }

        final Cursor mCursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA}, null, null, "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

        int count = mCursor.getCount();

        String[] songs = new String[count];
        mAudioPath = new String[count];
        int i = 0;
        if (mCursor.moveToFirst()) {
            do {
                songs[i] = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                mAudioPath[i] = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                i++;
            } while (mCursor.moveToNext());
        }

        mCursor.close();

        return songs;
    }

    private void playSong(String path) throws IllegalArgumentException,
            IllegalStateException, IOException {

        Log.d("ringtone", "playSong :: " + path);

        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(path);
        mMediaPlayer.prepare();
        mMediaPlayer.start();
        ((ArrayAdapter) mListView.getAdapter()).notifyDataSetChanged();
    }

    private void pauseSong() {
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.pause();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseSong();
    }

    private void playSong() {
        if (!mMediaPlayer.isPlaying())
            mMediaPlayer.start();
    }

    private void nextSong() {
        if ((currentIndex + 1) < mAudioPath.length) {
            currentIndex++;
            ((ArrayAdapter) mListView.getAdapter()).notifyDataSetChanged();
            try {
                playSong(mAudioPath[currentIndex]);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void previousSong() {
        if ((currentIndex - 1) >= 0) {
            currentIndex--;
            ((ArrayAdapter) mListView.getAdapter()).notifyDataSetChanged();
            try {
                playSong(mAudioPath[currentIndex]);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}