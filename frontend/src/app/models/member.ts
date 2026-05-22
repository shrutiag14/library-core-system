export type MemberStatus = 'ACTIVE' | 'INACTIVE';

export interface Member {
  id: number;
  name: string;
  email: string;
  status: MemberStatus;
  createdAt: string;
  updatedAt: string;
}

export type MemberPayload = Pick<Member, 'name' | 'email' | 'status'>;
