export type LoanStatus = 'ISSUED' | 'RETURNED' | 'OVERDUE';

export interface Loan {
  id: number;
  bookId: number;
  bookTitle: string;
  bookIsbn: string;
  memberId: number;
  memberName: string;
  memberEmail: string;
  issuedAt: string;
  dueDate: string;
  returnedAt?: string;
  status: LoanStatus;
  overdueDays: number;
  fineAmount: number;
}

export interface IssuePayload {
  bookId: number;
  memberId: number;
  dueDate?: string;
}

export interface Dashboard {
  books: number;
  members: number;
  activeLoans: number;
  overdueLoans: number;
  outstandingFines: number;
}
