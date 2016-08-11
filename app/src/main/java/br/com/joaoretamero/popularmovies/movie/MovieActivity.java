package br.com.joaoretamero.popularmovies.movie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import br.com.joaoretamero.popularmovies.R;
import br.com.joaoretamero.popularmovies.model.Movie;
import br.com.joaoretamero.popularmovies.model.Video;
import butterknife.BindView;

public class MovieActivity extends AppCompatActivity implements MovieView {

    public static final String EXTRA_MOVIE_ID = "movie_id";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.movie_backdrop)
    ImageView backdrop;

    @BindView(R.id.movie_title)
    TextView title;

    @BindView(R.id.movie_vote_average)
    AppCompatRatingBar ratingBar;

    @BindView(R.id.movie_genres)
    TextView genres;

    @BindView(R.id.movie_duration)
    TextView duration;

    @BindView(R.id.movie_overview)
    TextView overview;

    @BindView(R.id.movie_production_companies)
    TextView productionCompanies;

    @BindView(R.id.movie_videos_list)
    RecyclerView videosList;

    private VideoAdapter videoAdapter;
    private MoviePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        initToolbar();
        initRatingBar();
        initVideosList();

        presenter = new MoviePresenter(this);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
    }

    private void initRatingBar() {
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1f);
        ratingBar.setRating(0f);
    }

    private void initVideosList() {
        videoAdapter = new VideoAdapter(MovieActivity.this);

        videosList.setLayoutManager(new LinearLayoutManager(MovieActivity.this, LinearLayoutManager.VERTICAL, false));
        videosList.setItemAnimator(new DefaultItemAnimator());
        videosList.setAdapter(videoAdapter);
    }

    private int getMovieIdFromIntent() {
        int movieId = 0;

        Intent intent = getIntent();
        if (intent != null) {
            movieId = intent.getIntExtra(EXTRA_MOVIE_ID, 0);
        }

        return movieId;
    }

    @Override
    protected void onStart() {
        super.onStart();

        presenter.start(getMovieIdFromIntent());
    }

    @Override
    public void bindData(Movie movie) {
        // TODO revisar
//        RealmResults<Genre> genresList = movie.genres.where().findAll();
//        RealmResults<ProductionCompany> productionCompaniesList = movie.productionCompanies.where().findAll();
//
//        title.setText(movie.title);
//        ratingBar.setRating(convert10StarsValueTo5StarsValue(movie.voteAverage));
//        genres.setText("None: " + genresList.size());
//        duration.setText(movie.runtime + " min");
//        overview.setText(movie.overview);
//        productionCompanies.setText("None: " + productionCompaniesList.size());
    }

    @Override
    public void setVideoList(List<Video> videos) {
        videoAdapter.updateData(videos);
    }

    private float convert10StarsValueTo5StarsValue(float tenStarsValue) {
        return (tenStarsValue * 10) / (100 * 5);
    }
}
