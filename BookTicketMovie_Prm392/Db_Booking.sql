

-- Tạo cơ sở dữ liệu
CREATE DATABASE MovieTicketBookingSystem;
GO

-- Sử dụng cơ sở dữ liệu vừa tạo
USE MovieTicketBookingSystem;
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
    Rating DECIMAL(2, 1) -- Ví dụ: 8.5 (Đây sẽ là rating tổng hợp từ bảng Vote)
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
  
    FOREIGN KEY (BookingID) REFERENCES Booking(BookingID) ON DELETE CASCADE ON UPDATE NO ACTION,
  
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