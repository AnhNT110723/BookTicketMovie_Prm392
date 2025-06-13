

-- =====================================================
-- MOVIE TICKET BOOKING SYSTEM DATABASE SETUP
-- =====================================================
-- This script will drop and recreate the database if it exists
-- Author: PRM392 Team
-- Date: June 2025
-- =====================================================

-- Switch to master database first to avoid connection issues
USE master;
GO

-- Check if database exists and drop it if it does
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'MovieTicketBookingSystem')
BEGIN
    PRINT 'Database MovieTicketBookingSystem exists. Dropping it...'
    
    -- Close all connections to the database
    ALTER DATABASE MovieTicketBookingSystem SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    
    -- Drop the database
    DROP DATABASE MovieTicketBookingSystem;
    PRINT 'Database dropped successfully.'
END
ELSE
BEGIN
    PRINT 'Database MovieTicketBookingSystem does not exist.'
END
GO

-- Create new database
PRINT 'Creating new database MovieTicketBookingSystem...'
CREATE DATABASE MovieTicketBookingSystem;
PRINT 'Database created successfully.'
GO

-- Use the newly created database
USE MovieTicketBookingSystem;
PRINT 'Using MovieTicketBookingSystem database.'
GO

-- Bảng Role (Phân quyền)
CREATE TABLE Role (
    RoleID INT PRIMARY KEY IDENTITY(1,1),
    RoleName NVARCHAR(50) UNIQUE NOT NULL,
    Description NVARCHAR(255) NULL
);
GO

-- Insert default roles (optional, but good for a fresh setup)
INSERT INTO Role (RoleName, Description) VALUES
('Admin', N'Quản trị viên hệ thống'),
('Customer', N'Người dùng thông thường'),
('FrontDeskOfficer', N'Nhân viên quầy vé');
GO

-- Bảng Người dùng (User)
CREATE TABLE [User] (
    UserID INT PRIMARY KEY IDENTITY(1,1),
    Name NVARCHAR(255) NOT NULL,
    Email NVARCHAR(255) UNIQUE NOT NULL,
    Phone NVARCHAR(20),
    PasswordHash NVARCHAR(255) NOT NULL,
    LoyaltyPoints DECIMAL(10, 2) DEFAULT 0.00,
    RegistrationDate DATETIME DEFAULT GETDATE(),
    IsActive BIT DEFAULT 1,
    RoleID INT NOT NULL DEFAULT 2 -- Default to 'Customer' (assuming RoleID 2 is Customer)
);
GO

-- Add FK for RoleID in User table
ALTER TABLE [User]
ADD CONSTRAINT FK_User_Role FOREIGN KEY (RoleID) REFERENCES Role(RoleID) ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Bảng Thành phố (City)
CREATE TABLE City (
    CityID INT PRIMARY KEY IDENTITY(1,1),
    Name NVARCHAR(255) UNIQUE NOT NULL
);
GO

-- Bảng Rạp chiếu (Cinema)
CREATE TABLE Cinema (
    CinemaID INT PRIMARY KEY IDENTITY(1,1),
    Name NVARCHAR(255) NOT NULL,
    Address NVARCHAR(500) NOT NULL,
    CityID INT NOT NULL,
    ContactInfo NVARCHAR(255),
    FOREIGN KEY (CityID) REFERENCES City(CityID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

-- Bảng Phòng chiếu (CinemaHall)
CREATE TABLE CinemaHall (
    HallID INT PRIMARY KEY IDENTITY(1,1),
    CinemaID INT NOT NULL,
    Name NVARCHAR(255) NOT NULL,
    TotalSeats INT NOT NULL,
    FOREIGN KEY (CinemaID) REFERENCES Cinema(CinemaID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

-- Bảng Phim (Movie)
CREATE TABLE Movie (
    MovieID INT PRIMARY KEY IDENTITY(1,1),
    Title NVARCHAR(500) NOT NULL,
    Description NVARCHAR(MAX),
    Duration INT NOT NULL, -- Thời lượng phim tính bằng phút
    Language NVARCHAR(50),
    ReleaseDate DATE,
    TrailerURL NVARCHAR(500),
    PosterURL NVARCHAR(500),
    Rating DECIMAL(2, 1), -- Ví dụ: 8.5 (Đây sẽ là rating tổng hợp từ bảng Vote)
    Director NVARCHAR(255), -- Add director field for compatibility
    Genre NVARCHAR(255), -- Add genre field for compatibility  
    Price DECIMAL(10, 2) DEFAULT 100000.00, -- Add price field
    IsActive BIT DEFAULT 1, -- Add isActive field
    IsTrending BIT DEFAULT 0, -- Add isTrending field
    CreatedDate DATETIME DEFAULT GETDATE() -- Add createdDate field
);
GO

-- Bảng Người (Person) - Dùng cho Đạo diễn và Diễn viên
CREATE TABLE Person (
    PersonID INT PRIMARY KEY IDENTITY(1,1),
    Name NVARCHAR(255) NOT NULL,
    DateOfBirth DATE NULL,
    Biography NVARCHAR(MAX) NULL,
    Nationality NVARCHAR(100) NULL,
    PhotoURL NVARCHAR(500) NULL
);
GO

-- Bảng trung gian MovieDirector (Phim - Đạo diễn)
CREATE TABLE MovieDirector (
    MovieDirectorID INT PRIMARY KEY IDENTITY(1,1),
    MovieID INT NOT NULL,
    PersonID INT NOT NULL, -- PersonID của đạo diễn
    FOREIGN KEY (MovieID) REFERENCES Movie(MovieID) ON DELETE CASCADE ON UPDATE NO ACTION,
    FOREIGN KEY (PersonID) REFERENCES Person(PersonID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    UNIQUE (MovieID, PersonID) -- Đảm bảo một đạo diễn chỉ được gán một lần cho một phim
);
GO

-- Bảng trung gian MovieActor (Phim - Diễn viên)
CREATE TABLE MovieActor (
    MovieActorID INT PRIMARY KEY IDENTITY(1,1),
    MovieID INT NOT NULL,
    PersonID INT NOT NULL, -- PersonID của diễn viên
    RoleName NVARCHAR(255) NULL, -- Tên vai diễn (ví dụ: "John Wick", "Neo")
    FOREIGN KEY (MovieID) REFERENCES Movie(MovieID) ON DELETE CASCADE ON UPDATE NO ACTION,
    FOREIGN KEY (PersonID) REFERENCES Person(PersonID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    UNIQUE (MovieID, PersonID) -- Đảm bảo một diễn viên chỉ được gán một lần cho một phim
);
GO

-- Bảng Thể loại (Genre)
CREATE TABLE Genre (
    GenreID INT PRIMARY KEY IDENTITY(1,1),
    Name NVARCHAR(100) UNIQUE NOT NULL
);
GO

-- Bảng trung gian MovieGenre (Phim - Thể loại)
CREATE TABLE MovieGenre (
    MovieGenreID INT PRIMARY KEY IDENTITY(1,1),
    MovieID INT NOT NULL,
    GenreID INT NOT NULL,
    FOREIGN KEY (MovieID) REFERENCES Movie(MovieID) ON DELETE CASCADE ON UPDATE NO ACTION,
    FOREIGN KEY (GenreID) REFERENCES Genre(GenreID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    UNIQUE (MovieID, GenreID) -- Đảm bảo một thể loại chỉ được gán một lần cho một phim
);
GO

-- Bảng Suất chiếu (Show)
CREATE TABLE Show (
    ShowID INT PRIMARY KEY IDENTITY(1,1),
    MovieID INT NOT NULL,
    HallID INT NOT NULL,
    StartTime DATETIME NOT NULL,
    EndTime DATETIME NOT NULL,
    TicketPrice DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (MovieID) REFERENCES Movie(MovieID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY (HallID) REFERENCES CinemaHall(HallID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

-- Bảng Ghế (Seat)
CREATE TABLE Seat (
    SeatID INT PRIMARY KEY IDENTITY(1,1),
    HallID INT NOT NULL,
    RowNumber NVARCHAR(10) NOT NULL,
    ColumnNumber INT NOT NULL,
    SeatType NVARCHAR(50) NOT NULL, -- Ví dụ: 'Regular', 'VIP', 'Premium', 'Accessible'
    FOREIGN KEY (HallID) REFERENCES CinemaHall(HallID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    UNIQUE (HallID, RowNumber, ColumnNumber) -- Đảm bảo mỗi ghế trong một phòng chiếu là duy nhất
);
GO


-- Bảng Đặt vé (Booking)
CREATE TABLE Booking (
    BookingID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    ShowID INT NOT NULL,
    BookingTime DATETIME DEFAULT GETDATE(),
    NumberOfSeats INT NOT NULL,
    TotalPrice DECIMAL(10, 2) NOT NULL,
    Status NVARCHAR(50) NOT NULL, -- Ví dụ: 'Pending', 'Confirmed', 'Cancelled', 'Refunded'
    QRCodeData NVARCHAR(MAX),
    FOREIGN KEY (UserID) REFERENCES [User](UserID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY (ShowID) REFERENCES Show(ShowID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

-- Bảng Ghế Đặt (BookedSeat) - Bảng trung gian cho mối quan hệ N:M giữa Booking và Seat cho một Show cụ thể
CREATE TABLE BookedSeat (
    BookedSeatID INT PRIMARY KEY IDENTITY(1,1),
    BookingID INT NOT NULL,
    SeatID INT NOT NULL,
    ShowID INT NOT NULL, -- Thêm ShowID để tối ưu truy vấn kiểm tra ghế trống cho suất chiếu
    IsReserved BIT DEFAULT 1 NOT NULL, -- Mặc định là đã đặt
    ReservedByUserID INT, -- ID của người dùng đã đặt ghế, quan trọng cho kiểm soát đồng thời lạc quan
    ReservationTimestamp DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (BookingID) REFERENCES Booking(BookingID) ON DELETE CASCADE ON UPDATE NO ACTION,
    FOREIGN KEY (SeatID) REFERENCES Seat(SeatID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY (ShowID) REFERENCES Show(ShowID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    UNIQUE (SeatID, ShowID) -- Đảm bảo một ghế chỉ có thể được đặt một lần cho một suất chiếu cụ thể
);
GO

-- Bảng Thanh toán (Payment)
CREATE TABLE Payment (
    PaymentID INT PRIMARY KEY IDENTITY(1,1),
    BookingID INT UNIQUE NOT NULL, -- UNIQUE để đảm bảo mối quan hệ 1:1 với Booking
    Amount DECIMAL(10, 2) NOT NULL,
    PaymentTime DATETIME DEFAULT GETDATE(),
    PaymentStatus NVARCHAR(50) NOT NULL, -- Ví dụ: 'Pending', 'Completed', 'Failed', 'Refunded'
    PaymentMethod NVARCHAR(50) NOT NULL, -- Ví dụ: 'CreditCard', 'Cash', 'Momo', 'ZaloPay', 'VNPAY'
    TransactionID NVARCHAR(255), -- Mã giao dịch từ cổng thanh toán
    FOREIGN KEY (BookingID) REFERENCES Booking(BookingID) ON DELETE CASCADE ON UPDATE NO ACTION
);
GO

-- Bảng Vote (Đánh giá sao)
CREATE TABLE Vote (
    VoteID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    MovieID INT NOT NULL,
    RatingValue INT NOT NULL CHECK (RatingValue >= 1 AND RatingValue <= 5), -- Giá trị đánh giá từ 1 đến 5 sao
    VoteTime DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (UserID) REFERENCES [User](UserID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY (MovieID) REFERENCES Movie(MovieID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    UNIQUE (UserID, MovieID) -- Mỗi người dùng chỉ có thể vote một lần cho mỗi phim
);
GO

-- Bảng Comment (Bình luận)
CREATE TABLE Comment (
    CommentID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    MovieID INT NOT NULL,
    CommentText NVARCHAR(MAX) NOT NULL,
    CommentTime DATETIME DEFAULT GETDATE(),
    ParentCommentID INT NULL, -- Dành cho bình luận phản hồi (reply), NULL nếu là bình luận gốc
    IsApproved BIT DEFAULT 0, -- Trạng thái kiểm duyệt (0: chưa duyệt, 1: đã duyệt)
    FOREIGN KEY (UserID) REFERENCES [User](UserID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY (MovieID) REFERENCES Movie(MovieID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY (ParentCommentID) REFERENCES Comment(CommentID) ON DELETE NO ACTION ON UPDATE NO ACTION -- Tự tham chiếu
);
GO

-- Bảng MovieFavorite (Phim yêu thích)
CREATE TABLE MovieFavorite (
    MovieFavoriteID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    MovieID INT NOT NULL,
    FavoriteTime DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (UserID) REFERENCES [User](UserID) ON DELETE CASCADE ON UPDATE NO ACTION,
    FOREIGN KEY (MovieID) REFERENCES Movie(MovieID) ON DELETE CASCADE ON UPDATE NO ACTION,
    UNIQUE (UserID, MovieID) -- Mỗi người dùng chỉ có thể đánh dấu một phim là yêu thích một lần
);
GO

-- Bảng Tokens (Quản lý token xác thực người dùng)
CREATE TABLE Tokens (
    TokenID BIGINT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    TokenValue NVARCHAR(255) UNIQUE NOT NULL, -- Lưu hash hoặc mã hóa của token
    TokenType NVARCHAR(50) NOT NULL, -- Ví dụ: 'access', 'refresh', 'password_reset'
    IssuedAt DATETIME NOT NULL DEFAULT GETDATE(),
    ExpiresAt DATETIME NOT NULL,
    IsRevoked BIT NOT NULL DEFAULT 0,
    LastUsedAt DATETIME NULL,
    Metadata NVARCHAR(MAX) NULL, -- Lưu thông tin JSON như IP, User-Agent
    Version INT NOT NULL DEFAULT 1,
    FOREIGN KEY (UserID) REFERENCES [User](UserID) ON DELETE CASCADE ON UPDATE NO ACTION
);
GO



-- Tạo chỉ mục để tối ưu hóa hiệu suất truy vấn
CREATE INDEX IX_Cinema_CityID ON Cinema (CityID);
CREATE INDEX IX_CinemaHall_CinemaID ON CinemaHall (CinemaID);
CREATE INDEX IX_Show_MovieID ON Show (MovieID);
CREATE INDEX IX_Show_HallID ON Show (HallID);
CREATE INDEX IX_Show_StartTime ON Show (StartTime);
CREATE INDEX IX_Seat_HallID ON Seat (HallID);
CREATE INDEX IX_Booking_UserID ON Booking (UserID);
CREATE INDEX IX_Booking_ShowID ON Booking (ShowID);
CREATE INDEX IX_BookedSeat_BookingID ON BookedSeat (BookingID);
CREATE INDEX IX_BookedSeat_SeatID ON BookedSeat (SeatID);
CREATE INDEX IX_BookedSeat_ShowID ON BookedSeat (ShowID);
CREATE INDEX IX_Payment_BookingID ON Payment (BookingID);
CREATE INDEX IX_Movie_Title ON Movie (Title);
CREATE INDEX IX_Movie_ReleaseDate ON Movie (ReleaseDate);
CREATE INDEX IX_User_Email ON [User] (Email);
CREATE INDEX IX_User_RoleID ON [User] (RoleID);
CREATE INDEX IX_Vote_UserID ON Vote (UserID);
CREATE INDEX IX_Vote_MovieID ON Vote (MovieID);
CREATE INDEX IX_Comment_UserID ON Comment (UserID);
CREATE INDEX IX_Comment_MovieID ON Comment (MovieID);
CREATE INDEX IX_Comment_ParentCommentID ON Comment (ParentCommentID);
CREATE INDEX IX_MovieFavorite_UserID ON MovieFavorite (UserID);
CREATE INDEX IX_MovieFavorite_MovieID ON MovieFavorite (MovieID);
CREATE INDEX IX_Person_Name ON Person (Name);
CREATE INDEX IX_MovieDirector_MovieID ON MovieDirector (MovieID);
CREATE INDEX IX_MovieDirector_PersonID ON MovieDirector (PersonID);
CREATE INDEX IX_MovieActor_MovieID ON MovieActor (MovieID);
CREATE INDEX IX_MovieActor_PersonID ON MovieActor (PersonID);
CREATE INDEX IX_Genre_Name ON Genre (Name);
CREATE INDEX IX_MovieGenre_MovieID ON MovieGenre (MovieID);
CREATE INDEX IX_MovieGenre_GenreID ON MovieGenre (GenreID);
-- Chỉ mục cho bảng Tokens
CREATE INDEX IX_Tokens_UserID ON Tokens (UserID);
CREATE INDEX IX_Tokens_ExpiresAt ON Tokens (ExpiresAt);
CREATE INDEX IX_Tokens_IsRevoked ON Tokens (IsRevoked);
CREATE INDEX IX_Tokens_TokenType ON Tokens (TokenType);

GO

-- =====================================================
-- SAMPLE DATA INSERTION
-- =====================================================
PRINT 'Inserting sample data...'

-- Sample Cities
INSERT INTO City (Name) VALUES 
(N'Hồ Chí Minh'),
(N'Hà Nội'),
(N'Đà Nẵng'),
(N'Cần Thơ'),
(N'Hải Phòng');

-- Sample Genres
INSERT INTO Genre (Name) VALUES 
(N'Action'),
(N'Comedy'),
(N'Drama'),
(N'Horror'),
(N'Romance'),
(N'Sci-Fi'),
(N'Thriller'),
(N'Animation'),
(N'Adventure'),
(N'Fantasy');

-- Sample People (Directors and Actors)
INSERT INTO Person (Name, DateOfBirth, Biography, Nationality, PhotoURL) VALUES 
(N'Christopher Nolan', '1970-07-30', N'British-American film director, producer, and screenwriter', N'British', 'https://example.com/nolan.jpg'),
(N'Quentin Tarantino', '1963-03-27', N'American film director, screenwriter, and producer', N'American', 'https://example.com/tarantino.jpg'),
(N'Leonardo DiCaprio', '1974-11-11', N'American actor and film producer', N'American', 'https://example.com/dicaprio.jpg'),
(N'Scarlett Johansson', '1984-11-22', N'American actress and singer', N'American', 'https://example.com/johansson.jpg'),
(N'Robert Downey Jr.', '1965-04-04', N'American actor and producer', N'American', 'https://example.com/rdj.jpg'),
(N'Ngô Thanh Vân', '1979-02-26', N'Vietnamese actress, singer, and model', N'Vietnamese', 'https://example.com/ngothanhvan.jpg'),
(N'Trấn Thành', '1987-02-05', N'Vietnamese comedian, actor, and TV host', N'Vietnamese', 'https://example.com/tranthanh.jpg');

-- Sample Movies
INSERT INTO Movie (Title, Description, Duration, Language, ReleaseDate, TrailerURL, PosterURL, Rating, Director, Genre, Price, IsActive, IsTrending) VALUES 
-- Original sample movies
(N'Inception', N'A thief who steals corporate secrets through dream-sharing technology', 148, N'English', '2010-07-16', 'https://youtube.com/inception-trailer', 'https://m.media-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_SX300.jpg', 8.8, N'Christopher Nolan', N'Sci-Fi, Thriller', 120000.00, 1, 1),
(N'Pulp Fiction', N'The lives of two mob hitmen, a boxer, and a gangster intertwine', 154, N'English', '1994-10-14', 'https://youtube.com/pulpfiction-trailer', 'https://m.media-amazon.com/images/M/MV5BNGNhMDIzZTUtNTBlZi00MTRlLWFjM2ItYzViMjE3YzI5MjljXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_SX300.jpg', 8.9, N'Quentin Tarantino', N'Action, Drama', 110000.00, 1, 0),
(N'The Avengers', N'Superheroes team up to save the world', 143, N'English', '2012-05-04', 'https://youtube.com/avengers-trailer', 'https://m.media-amazon.com/images/M/MV5BNDYxNjQyMjAtNTdiOS00NGYwLWFmNTAtNThmYjU5ZGI2YTI1XkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_SX300.jpg', 8.0, N'Joss Whedon', N'Action, Adventure', 150000.00, 1, 1),
(N'Bố Già', N'Vietnamese family comedy drama', 128, N'Vietnamese', '2021-03-12', 'https://youtube.com/bogia-trailer', 'https://i.imgur.com/QvKGF5Y.jpg', 7.5, N'Trấn Thành', N'Comedy, Drama', 100000.00, 1, 0),
(N'Hai Phượng', N'Vietnamese action thriller', 98, N'Vietnamese', '2019-02-22', 'https://youtube.com/haiphuong-trailer', 'https://i.imgur.com/8Y3V9ZX.jpg', 6.8, N'Ngô Thanh Vân', N'Action, Thriller', 95000.00, 1, 0),

-- Trending Movies from HomeActivity
(N'Avatar: The Way of Water', N'Set more than a decade after the events of the first film...', 192, N'English', '2022-12-16', 'https://youtube.com/avatar2-trailer', 'https://m.media-amazon.com/images/M/MV5BYjhiNjBlODctY2ZiOC00YjVlLWFlNzAtNTVhNzM1YjI1NzMxXkEyXkFqcGdeQXVyMjQxNTE1MDA@._V1_SX300.jpg', 7.6, N'James Cameron', N'Action, Adventure, Sci-Fi', 180000.00, 1, 1),
(N'Top Gun: Maverick', N'After thirty years, Maverick is still pushing the envelope...', 130, N'English', '2022-05-27', 'https://youtube.com/topgun-trailer', 'https://m.media-amazon.com/images/M/MV5BZWYzOGEwNTgtNWU3NS00ZTQ0LWJkODUtMmVhMjIwMjA1ZmQwXkEyXkFqcGdeQXVyMjkwOTAyMDU@._V1_SX300.jpg', 8.3, N'Joseph Kosinski', N'Action, Drama', 160000.00, 1, 1),
(N'Black Panther: Wakanda Forever', N'Queen Ramonda, Shuri, M''Baku, Okoye and the Dora Milaje...', 161, N'English', '2022-11-11', 'https://youtube.com/blackpanther2-trailer', 'https://m.media-amazon.com/images/M/MV5BNTM4NjIxNmEtYWE5NS00NDczLTkyNWQtYThhNmQyZGQzMjM0XkEyXkFqcGdeQXVyODk4OTc3MTY@._V1_SX300.jpg', 6.7, N'Ryan Coogler', N'Action, Adventure, Drama', 170000.00, 1, 1),
(N'Spider-Man: No Way Home', N'With Spider-Man''s identity now revealed, Peter asks Doctor Strange for help...', 148, N'English', '2021-12-17', 'https://youtube.com/spiderman-trailer', 'https://m.media-amazon.com/images/M/MV5BZWMyYzFjYTYtNTRjYi00OGExLWE2YzgtOGRmYjAxZTU3NzBiXkEyXkFqcGdeQXVyMzQ0MzA0NTM@._V1_SX300.jpg', 8.4, N'Jon Watts', N'Action, Adventure, Sci-Fi', 165000.00, 1, 1),

-- Featured Movies from HomeActivity
(N'The Batman', N'When a sadistic serial killer begins murdering key political figures in Gotham...', 176, N'English', '2022-03-04', 'https://youtube.com/batman-trailer', 'https://m.media-amazon.com/images/M/MV5BMDdmMTBiNTYtMDIzNi00NGVlLWIzMDYtZTk3MTQ3NGQxZGEwXkEyXkFqcGdeQXVyMzMwOTU5MDk@._V1_SX300.jpg', 7.8, N'Matt Reeves', N'Action, Crime, Drama', 155000.00, 1, 0),
(N'Doctor Strange in the Multiverse of Madness', N'Doctor Strange teams up with a mysterious teenage girl...', 126, N'English', '2022-05-06', 'https://youtube.com/doctorstrange2-trailer', 'https://m.media-amazon.com/images/M/MV5BNWM0ZGJlMzMtZmYwMi00NzI3LWI0Y2EtYzY5Yjk4OWJhN2Q4XkEyXkFqcGdeQXVyMTM1MTE1NDMx@._V1_SX300.jpg', 6.9, N'Sam Raimi', N'Action, Adventure, Fantasy', 145000.00, 1, 0),
(N'Minions: The Rise of Gru', N'A fanboy of a supervillain supergroup known as the Vicious 6...', 87, N'English', '2022-07-01', 'https://youtube.com/minions-trailer', 'https://m.media-amazon.com/images/M/MV5BZTAzMTkyNmQtNTMzZS00MTM1LWI4YzEtMjVlYjU0ZWI5Y2IzXkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg', 6.5, N'Kyle Balda', N'Animation, Adventure, Comedy', 125000.00, 1, 0),
(N'Thor: Love and Thunder', N'Thor enlists the help of Valkyrie, Korg and ex-girlfriend Jane Foster...', 119, N'English', '2022-07-08', 'https://youtube.com/thor4-trailer', 'https://m.media-amazon.com/images/M/MV5BZjRiMDhiZjQtNjk5Yi00ZDcwLTkyYTEtMDc1NjdmNjFhNGIzXkEyXkFqcGc@._V1_QL75_UX380_CR0,0,380,562_.jpg', 6.2, N'Taika Waititi', N'Action, Adventure, Comedy', 140000.00, 1, 0);

-- Sample Movie Directors
INSERT INTO MovieDirector (MovieID, PersonID) VALUES 
(1, 1), -- Inception - Christopher Nolan
(2, 2), -- Pulp Fiction - Quentin Tarantino
(3, 1), -- The Avengers - Christopher Nolan (example, should be Joss Whedon)
(4, 7), -- Bố Già - Trấn Thành
(5, 6), -- Hai Phượng - Ngô Thanh Vân
(6, 1), -- Avatar 2 - James Cameron (using Christopher Nolan as placeholder)
(7, 1), -- Top Gun Maverick - Joseph Kosinski (using Christopher Nolan as placeholder)
(8, 1), -- Black Panther 2 - Ryan Coogler (using Christopher Nolan as placeholder)
(9, 1), -- Spider-Man - Jon Watts (using Christopher Nolan as placeholder)
(10, 1), -- The Batman - Matt Reeves (using Christopher Nolan as placeholder)
(11, 2), -- Doctor Strange 2 - Sam Raimi (using Tarantino as placeholder)
(12, 1), -- Minions - Kyle Balda (using Christopher Nolan as placeholder)
(13, 2); -- Thor 4 - Taika Waititi (using Tarantino as placeholder)

-- Sample Movie Actors
INSERT INTO MovieActor (MovieID, PersonID, RoleName) VALUES 
(1, 3, N'Dom Cobb'), -- Inception - Leonardo DiCaprio
(2, 3, N'Vincent Vega'), -- Pulp Fiction - Leonardo DiCaprio (example)
(3, 5, N'Tony Stark / Iron Man'), -- The Avengers - Robert Downey Jr.
(3, 4, N'Black Widow'), -- The Avengers - Scarlett Johansson
(4, 7, N'Ba Sang'), -- Bố Già - Trấn Thành
(5, 6, N'Hai Phượng'); -- Hai Phượng - Ngô Thanh Vân

-- Sample Movie Genres
INSERT INTO MovieGenre (MovieID, GenreID) VALUES 
(1, 6), -- Inception - Sci-Fi
(1, 7), -- Inception - Thriller
(2, 1), -- Pulp Fiction - Action
(2, 3), -- Pulp Fiction - Drama
(3, 1), -- The Avengers - Action
(3, 9), -- The Avengers - Adventure
(4, 2), -- Bố Già - Comedy
(4, 3), -- Bố Già - Drama
(5, 1), -- Hai Phượng - Action
(5, 7); -- Hai Phượng - Thriller

-- Sample Cinemas
INSERT INTO Cinema (Name, Address, CityID, ContactInfo) VALUES 
(N'CGV Vincom Center', N'72 Lê Thánh Tôn, Quận 1, TP.HCM', 1, N'028-3822-5678'),
(N'Galaxy Cinema Nguyễn Du', N'116 Nguyễn Du, Quận 1, TP.HCM', 1, N'028-3827-1717'),
(N'Lotte Cinema Landmark', N'720A Điện Biên Phủ, Quận 3, TP.HCM', 1, N'028-3930-0060'),
(N'CGV Vincom Bà Triệu', N'191 Bà Triệu, Hai Bà Trưng, Hà Nội', 2, N'024-3974-3333'),
(N'Galaxy Cinema Linh Đàm', N'Shopping Mall, Hoàng Mai, Hà Nội', 2, N'024-3632-4896');

-- Sample Cinema Halls
INSERT INTO CinemaHall (CinemaID, Name, TotalSeats) VALUES 
(1, N'Phòng 1', 120),
(1, N'Phòng 2', 100),
(1, N'Phòng VIP', 80),
(2, N'Rạp A', 150),
(2, N'Rạp B', 130),
(3, N'Hall 1', 200),
(3, N'Hall 2', 180),
(4, N'Phòng Gold', 90),
(5, N'Premium Hall', 110);

-- Sample Seats for Hall 1 (Cinema 1)
DECLARE @HallID INT = 1;
DECLARE @Row NVARCHAR(10);
DECLARE @Col INT;

-- Create seats for rows A-J, columns 1-12
DECLARE @RowNames TABLE (RowName NVARCHAR(10));
INSERT INTO @RowNames VALUES ('A'), ('B'), ('C'), ('D'), ('E'), ('F'), ('G'), ('H'), ('I'), ('J');

DECLARE row_cursor CURSOR FOR SELECT RowName FROM @RowNames;
OPEN row_cursor;
FETCH NEXT FROM row_cursor INTO @Row;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @Col = 1;
    WHILE @Col <= 12
    BEGIN
        INSERT INTO Seat (HallID, RowNumber, ColumnNumber, SeatType) 
        VALUES (@HallID, @Row, @Col, 
            CASE 
                WHEN @Row IN ('A', 'B') THEN 'VIP'
                WHEN @Row IN ('I', 'J') THEN 'Regular'
                ELSE 'Premium'
            END
        );
        SET @Col = @Col + 1;
    END
    FETCH NEXT FROM row_cursor INTO @Row;
END

CLOSE row_cursor;
DEALLOCATE row_cursor;

-- Sample Users (additional to the admin/customer roles already inserted)
INSERT INTO [User] (Name, Email, Phone, PasswordHash, LoyaltyPoints, RoleID) VALUES 
(N'Nguyễn Văn An', 'admin@moviebooking.com', '0901234567', 'hashed_password_admin', 0.00, 1), -- Admin
(N'Trần Thị Bích', 'bich.tran@gmail.com', '0912345678', 'hashed_password_bich', 150.50, 2), -- Customer
(N'Lê Minh Tuấn', 'tuan.le@yahoo.com', '0923456789', 'hashed_password_tuan', 75.25, 2), -- Customer
(N'Phạm Thu Hà', 'ha.pham@outlook.com', '0934567890', 'hashed_password_ha', 200.00, 2), -- Customer
(N'Hoàng Văn Dũng', 'dung.hoang@gmail.com', '0945678901', 'hashed_password_dung', 0.00, 3), -- Front Desk Officer
(N'Võ Thị Lan', 'lan.vo@company.com', '0956789012', 'hashed_password_lan', 320.75, 2), -- Customer
(N'TesterCustomer', 'customer@gmail.com', '12345678909', '48e863bdec3d48f7c18add0dc95d19bef8de9288053309702c701066a93de9b8', 320.75, 2), -- Customer
(N'TesterAdmin', 'admin@gmail.com', '12345678909', '48e863bdec3d48f7c18add0dc95d19bef8de9288053309702c701066a93de9b8', 320.75, 1), -- Admin
(N'TesterOfficer', 'fdofficer@gmail.com', '12345678909', '48e863bdec3d48f7c18add0dc95d19bef8de9288053309702c701066a93de9b8', 320.75, 3); -- Front Desh Officer


-- Sample Shows (Movie screenings)
INSERT INTO Show (MovieID, HallID, StartTime, EndTime, TicketPrice) VALUES 
-- Today's shows
(1, 1, DATEADD(HOUR, 2, GETDATE()), DATEADD(HOUR, 4, GETDATE()), 120000.00), -- Inception
(2, 2, DATEADD(HOUR, 3, GETDATE()), DATEADD(HOUR, 6, GETDATE()), 110000.00), -- Pulp Fiction
(3, 3, DATEADD(HOUR, 1, GETDATE()), DATEADD(HOUR, 3, GETDATE()), 150000.00), -- The Avengers
(4, 4, DATEADD(HOUR, 4, GETDATE()), DATEADD(HOUR, 6, GETDATE()), 100000.00), -- Bố Già
(5, 5, DATEADD(HOUR, 5, GETDATE()), DATEADD(HOUR, 7, GETDATE()), 95000.00),  -- Hai Phượng

-- Tomorrow's shows
(1, 1, DATEADD(DAY, 1, DATEADD(HOUR, 10, GETDATE())), DATEADD(DAY, 1, DATEADD(HOUR, 12, GETDATE())), 120000.00),
(2, 2, DATEADD(DAY, 1, DATEADD(HOUR, 14, GETDATE())), DATEADD(DAY, 1, DATEADD(HOUR, 17, GETDATE())), 110000.00),
(3, 3, DATEADD(DAY, 1, DATEADD(HOUR, 16, GETDATE())), DATEADD(DAY, 1, DATEADD(HOUR, 18, GETDATE())), 150000.00),
(4, 4, DATEADD(DAY, 1, DATEADD(HOUR, 19, GETDATE())), DATEADD(DAY, 1, DATEADD(HOUR, 21, GETDATE())), 100000.00),
(5, 5, DATEADD(DAY, 1, DATEADD(HOUR, 21, GETDATE())), DATEADD(DAY, 1, DATEADD(HOUR, 23, GETDATE())), 95000.00);

-- Sample Bookings
INSERT INTO Booking (UserID, ShowID, NumberOfSeats, TotalPrice, Status) VALUES 
(2, 1, 2, 240000.00, 'Confirmed'), -- Bích books Inception
(3, 2, 1, 110000.00, 'Confirmed'), -- Tuấn books Pulp Fiction
(4, 3, 3, 450000.00, 'Pending'),   -- Hà books The Avengers
(6, 4, 2, 200000.00, 'Confirmed'); -- Lan books Bố Già

-- Sample Booked Seats
INSERT INTO BookedSeat (BookingID, SeatID, ShowID, ReservedByUserID) VALUES 
(1, 1, 1, 2), -- Booking 1, Seat A1
(1, 2, 1, 2), -- Booking 1, Seat A2
(2, 13, 2, 3), -- Booking 2, Seat B1
(3, 25, 3, 4), -- Booking 3, Seat C1
(3, 26, 3, 4), -- Booking 3, Seat C2
(3, 27, 3, 4), -- Booking 3, Seat C3
(4, 37, 4, 6), -- Booking 4, Seat D1
(4, 38, 4, 6); -- Booking 4, Seat D2

-- Sample Payments
INSERT INTO Payment (BookingID, Amount, PaymentStatus, PaymentMethod, TransactionID) VALUES 
(1, 240000.00, 'Completed', 'VNPAY', 'VNP_20250610_001'),
(2, 110000.00, 'Completed', 'Momo', 'MOMO_20250610_002'),
(4, 200000.00, 'Completed', 'ZaloPay', 'ZALO_20250610_003');

-- Sample Votes/Ratings
INSERT INTO Vote (UserID, MovieID, RatingValue) VALUES 
(2, 1, 5), -- Bích rates Inception 5 stars
(2, 2, 4), -- Bích rates Pulp Fiction 4 stars
(3, 1, 5), -- Tuấn rates Inception 5 stars
(3, 3, 4), -- Tuấn rates The Avengers 4 stars
(4, 2, 3), -- Hà rates Pulp Fiction 3 stars
(6, 4, 5), -- Lan rates Bố Già 5 stars
(6, 5, 4); -- Lan rates Hai Phượng 4 stars

-- Sample Comments
INSERT INTO Comment (UserID, MovieID, CommentText, IsApproved) VALUES 
(2, 1, N'Phim rất hay! Cốt truyện phức tạp nhưng thú vị.', 1),
(3, 1, N'Leonardo DiCaprio diễn xuất tuyệt vời. Highly recommended!', 1),
(4, 2, N'Phim kinh điển của Tarantino. Must watch!', 1),
(6, 4, N'Phim Việt Nam hay nhất năm. Trấn Thành diễn rất tự nhiên.', 1),
(2, 3, N'Action scenes are amazing! Marvel never disappoints.', 1);

-- Sample Movie Favorites
INSERT INTO MovieFavorite (UserID, MovieID) VALUES 
(2, 1), -- Bích favorites Inception
(2, 4), -- Bích favorites Bố Già
(3, 1), -- Tuấn favorites Inception
(3, 3), -- Tuấn favorites The Avengers
(4, 2), -- Hà favorites Pulp Fiction
(6, 4), -- Lan favorites Bố Già
(6, 5); -- Lan favorites Hai Phượng

PRINT 'Sample data insertion completed successfully!'
PRINT 'Database setup complete with sample data for testing.'

GO