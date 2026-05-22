export interface AuditLog {
  id: number;
  actorEmail: string;
  action: string;
  entityType: string;
  entityId?: string;
  details?: string;
  createdAt: string;
}
