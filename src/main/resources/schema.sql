CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);
CREATE TABLE teams (
    room_id BIGINT NOT NULL,
    team_number INT NOT NULL,
    PRIMARY KEY (room_id, team_number)
);

CREATE TABLE team_members (
    room_id BIGINT NOT NULL,
    team_number INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    PRIMARY KEY (room_id, team_number, username)
);

CREATE TABLE team_preferences (
    room_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    preferred_username VARCHAR(50) NOT NULL,
    PRIMARY KEY (room_id, username)
);
