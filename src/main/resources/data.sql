INSERT INTO users (username, password, role) VALUES
    ('admin', '{bcrypt}$2y$10$ngxCDmuVK1TaGchiYQfJ1OAKkd64IH6skGsNw1sLabrTICOHPxC0e', 'ADMIN'),
    ('participant', '{bcrypt}$2y$10$ngxCDmuVK1TaGchiYQfJ1OAKkd64IH6skGsNw1sLabrTICOHPxC0e', 'PARTICIPANT'),
    ('participant2', '{bcrypt}$2y$10$ngxCDmuVK1TaGchiYQfJ1OAKkd64IH6skGsNw1sLabrTICOHPxC0e', 'PARTICIPANT');

INSERT INTO quiz_questions (
    question_text, option_a, option_b, option_c, option_d, correct_option
) VALUES (
    '日本の首都はどこ？', '大阪', '東京', '京都', '札幌', 2
);
INSERT INTO quiz_questions (
    question_text, option_a, option_b, option_c, option_d, correct_option
) VALUES (
    '最も太陽に近い惑星はどれ？', '火星', '地球', '金星', '水星', 4
);
INSERT INTO quiz_questions (
    question_text, option_a, option_b, option_c, option_d, correct_option
) VALUES (
    '国旗が四角形ではない国は？', 'ネパール', 'カンボジア', 'タイ', 'シンガポール', 1
);
