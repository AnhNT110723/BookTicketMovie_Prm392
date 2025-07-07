package com.example.bookingticketmove_prm392.database.dao;

import com.example.bookingticketmove_prm392.models.Comment;
import com.example.bookingticketmove_prm392.models.Feedback;
import com.example.bookingticketmove_prm392.models.Vote;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO extends BaseDAO{


    //Get all feedback by movie id
    public List<Feedback> getAllFeedbackByMovieId(int movieId) throws SQLException {
        List<Feedback>  feedbackList = new ArrayList<>();
        String query = "SELECT\n" +
                "    u.Name,\n" +
                "    COALESCE(c.UserID, v.UserID) AS UserID,\n" +
                "    COALESCE(c.MovieID, v.MovieID) AS MovieID,\n" +
                "    c.CommentText,\n" +
                "    v.RatingValue\n" +
                "   \n" +
                "FROM Comment c\n" +
                "FULL OUTER JOIN Vote v \n" +
                "    ON c.UserID = v.UserID AND c.MovieID = v.MovieID\n" +
                "LEFT JOIN [User] u \n" +
                "    ON COALESCE(c.UserID, v.UserID) = u.UserID\n" +
                "WHERE COALESCE(c.MovieID, v.MovieID) = ?\n" +
                "ORDER BY coalesce(c.CommentTime, v.VoteTime)";


        ResultSet rs = null;
        PreparedStatement statement = null;
        try{
            rs = executeQuery(query, movieId);
            while (rs.next()){
                Feedback feedback = mapResultSetToFeedback(rs);
                feedbackList.add(feedback);
            }
        }finally {
            closeResources(rs, statement);
        }
        return feedbackList;
    }

    //Create new comment
    public boolean addComment(Comment comment) throws SQLException{
        String query = "INSERT INTO Comment (UserID, MovieID, CommentText) VALUES (?, ?, ?)";

        int result = executeUpdate(query,
                comment.getUserId(),
                comment.getMovieId(),
                comment.getCommentText());
        return result > 0;
    }

    //Create new Vote
    public boolean addVote(Vote vote) throws SQLException{
        String query = "INSERT INTO Vote (UserID, MovieID, RatingValue) VALUES (?, ?, ?)";

        int result = executeUpdate(query,
                vote.getUserId(),
                vote.getMovieId(),
                vote.getRatingValue());
        return result > 0;
    }

    //Check user has vote before or not
    public boolean checkVote(int userId, int movieId) throws SQLException {
        String query = "SELECT COUNT(*) FROM Vote WHERE UserID = ? AND MovieID = ?";
        ResultSet rs = null;
        PreparedStatement statement = null;

        try {
            rs = executeQuery(query, userId, movieId);
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
                }
        } finally {
            closeResources(rs, statement);
        }
        return false;
    }

    //Helper method to map ResultSet to Feedback object
    private Feedback mapResultSetToFeedback(ResultSet rs) throws SQLException {
        Feedback feedback = new Feedback();
        feedback.setName(rs.getString("Name"));
        feedback.setUserId(rs.getInt("UserId"));
        feedback.setMovieId(rs.getInt("MovieId"));
        feedback.setCommentText(rs.getString("CommentText"));
        feedback.setRatingValue(rs.getInt("RatingValue"));
        return feedback;
    }
}
