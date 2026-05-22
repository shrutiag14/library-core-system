export interface Book {
  id: number;
  title: string;
  author: string;
  isbn: string;
  category?: string;
  totalCopies: number;
  availableCopies: number;
  shelfLocation?: string;
  createdAt: string;
  updatedAt: string;
}

export type BookPayload = Omit<Book, 'id' | 'createdAt' | 'updatedAt'>;
