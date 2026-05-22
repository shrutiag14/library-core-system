CREATE TABLE books (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  author VARCHAR(255) NOT NULL,
  isbn VARCHAR(32) NOT NULL,
  category VARCHAR(120),
  total_copies INT NOT NULL,
  available_copies INT NOT NULL,
  shelf_location VARCHAR(120),
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_books_isbn UNIQUE (isbn),
  INDEX idx_books_title (title),
  INDEX idx_books_author (author),
  INDEX idx_books_category (category),
  INDEX idx_books_deleted (deleted),
  CONSTRAINT chk_books_total_copies CHECK (total_copies >= 0),
  CONSTRAINT chk_books_available_copies CHECK (available_copies >= 0),
  CONSTRAINT chk_books_available_lte_total CHECK (available_copies <= total_copies)
);

CREATE TABLE members (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  status VARCHAR(16) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_members_email UNIQUE (email),
  INDEX idx_members_status (status)
);

CREATE TABLE loans (
  id BIGINT NOT NULL AUTO_INCREMENT,
  book_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  issued_at DATETIME(6) NOT NULL,
  due_date DATE NOT NULL,
  returned_at DATETIME(6),
  status VARCHAR(16) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books (id),
  CONSTRAINT fk_loans_member FOREIGN KEY (member_id) REFERENCES members (id),
  INDEX idx_loans_book_status (book_id, status),
  INDEX idx_loans_member_status (member_id, status),
  INDEX idx_loans_due_date (due_date)
);
