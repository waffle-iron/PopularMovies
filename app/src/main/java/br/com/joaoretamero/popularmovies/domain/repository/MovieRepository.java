package br.com.joaoretamero.popularmovies.domain.repository;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.com.joaoretamero.popularmovies.domain.json.GenreJson;
import br.com.joaoretamero.popularmovies.domain.json.MovieJson;
import br.com.joaoretamero.popularmovies.domain.json.MovieJsonResponse;
import br.com.joaoretamero.popularmovies.domain.json.VideoJson;
import br.com.joaoretamero.popularmovies.domain.local.Genre;
import br.com.joaoretamero.popularmovies.domain.local.Movie;
import br.com.joaoretamero.popularmovies.domain.local.Video;
import br.com.joaoretamero.popularmovies.domain.mapper.GenreMapper;
import br.com.joaoretamero.popularmovies.domain.mapper.MovieMapper;
import br.com.joaoretamero.popularmovies.domain.mapper.VideoMapper;
import br.com.joaoretamero.popularmovies.network.Network;
import br.com.joaoretamero.popularmovies.network.TheMovieDbService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieRepository {

    private static final String TAG = MovieRepository.class.getSimpleName();

    private ConnectivityManager connectivityManager;
    private TheMovieDbService theMovieDbService;

    public MovieRepository(ConnectivityManager connectivityManager) {
        this.connectivityManager = connectivityManager;
        this.theMovieDbService = Network.createTheMovieDbService();
    }

    public void findOne(final int movieId, final FindOneCallback findOneCallback) {
        Log.d(TAG, "findAll");
        if (isNetworkAvailable()) {
            Log.d(TAG, "newtork available");
            theMovieDbService.getMovie(movieId).enqueue(new Callback<MovieJson>() {
                @Override
                public void onResponse(Call<MovieJson> call, Response<MovieJson> response) {
                    Log.d(TAG, "request has response");
                    if (response.isSuccessful()) {
                        Log.d(TAG, "response was successfull");
                        mapOneAndUpdate(response.body());
                        findOneFromLocal(movieId, findOneCallback);
                    } else {
                        Log.d(TAG, "response wasnt successfull");
                        callFail(findOneCallback);
                    }
                }

                @Override
                public void onFailure(Call<MovieJson> call, Throwable t) {
                    callFail(findOneCallback);
                }
            });
        } else {
            Log.d(TAG, "newtork unavailable");
            findOneFromLocal(movieId, findOneCallback);
        }
    }

    private void findOneFromLocal(int movieId, FindOneCallback findOneCallback) {
        if (findOneCallback != null) {
            findOneCallback.onSuccess(Movie.findByMovieId(movieId));
        }
    }

    private void mapOneAndUpdate(MovieJson movieJson) {
        MovieMapper movieMapper = new MovieMapper();
        Movie movieFromNetwork = movieMapper.mapJsonToLocal(movieJson);
        Movie movie = updateMovie(movieFromNetwork);
        saveVideosFromMovie(movie, movieJson.videos);
        saveGenresFromMovie(movie, movieJson.genres);
    }

    public Movie updateMovie(Movie movieFromNetwork) {
        Movie movieLocal = Movie.findByMovieId(movieFromNetwork.movieId);
        movieLocal.voteAverage = movieFromNetwork.voteAverage;
        movieLocal.title = movieFromNetwork.title;
        movieLocal.poster = movieFromNetwork.poster;
        movieLocal.popularity = movieFromNetwork.popularity;
        movieLocal.backdrop = movieFromNetwork.backdrop;
        movieLocal.durationInMinutes = movieFromNetwork.durationInMinutes;
        movieLocal.overview = movieFromNetwork.overview;
        movieLocal.save();

        return movieLocal;
    }

    public void saveVideosFromMovie(Movie movie, List<VideoJson> videoJsonList) {
        if (videoJsonList == null || videoJsonList.size() == 0) {
            return;
        }

        VideoMapper videoMapper = new VideoMapper();
        List<Video> videos = videoMapper.mapJsonListToLocalList(videoJsonList);
        Video.clearAllFromMovie(movie.getId());
        Video.bulkInsert(videos);
    }

    public void saveGenresFromMovie(Movie movie, List<GenreJson> genreJsonList) {
        if (genreJsonList == null || genreJsonList.size() == 0) {
            return;
        }

        GenreMapper genreMapper = new GenreMapper();
        List<Genre> genres = genreMapper.mapJsonListToLocalList(genreJsonList);
        Genre.clearAllFromMovie(movie.getId());
        Genre.bulkInsert(genres);
    }

    public void findAll(final String sortOrder, final FindAllCallback findAllCallback) {
        if (isNetworkAvailable()) {
            theMovieDbService.getMovies().enqueue(new Callback<MovieJsonResponse>() {
                @Override
                public void onResponse(Call<MovieJsonResponse> call, Response<MovieJsonResponse> response) {
                    if (response.isSuccessful()) {
                        mapCleanAndInsert(response.body());
                        findAllFromLocal(sortOrder, findAllCallback);
                    } else {
                        callFail(findAllCallback);
                    }
                }

                @Override
                public void onFailure(Call<MovieJsonResponse> call, Throwable t) {
                    callFail(findAllCallback);
                }
            });
        } else {
            findAllFromLocal(sortOrder, findAllCallback);
        }
    }

    private void mapCleanAndInsert(MovieJsonResponse movieJsonResponse) {
        List<Movie> movieList = mapMovieJsonListToMovieLocalList(movieJsonResponse);
        Movie.clearAll();
        Movie.bulkInsert(movieList);
    }

    private List<Movie> mapMovieJsonListToMovieLocalList(MovieJsonResponse movieJsonResponse) {
        if (movieJsonResponse == null || movieJsonResponse.results == null) {
            return new ArrayList<Movie>();
        }

        MovieMapper mapper = new MovieMapper();
        return mapper.mapJsonListToLocalList(movieJsonResponse.results);
    }

    private void findAllFromLocal(String sortOrder, FindAllCallback findAllCallback) {
        if (findAllCallback != null) {
            findAllCallback.onSuccess(Movie.findAllSortedBy(sortOrder));
        }
    }

    private void callFail(DefaultCallback callback) {
        if (callback != null) {
            callback.onFail();
        }
    }

    private boolean isNetworkAvailable() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isAvailable();
    }

    private interface DefaultCallback {
        void onFail();
    }

    public interface FindAllCallback extends DefaultCallback {
        void onSuccess(List<Movie> movies);
    }

    public interface FindOneCallback extends DefaultCallback {
        void onSuccess(Movie movie);
    }
}
