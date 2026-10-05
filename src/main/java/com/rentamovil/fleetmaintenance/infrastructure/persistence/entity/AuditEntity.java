package com.rentamovil.fleetmaintenance.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// shared.audit: the audit table every service writes to (created by rtm-iam-db).
// fleet_app only has INSERT on it: rows are only appended here, never read or edited.
@Entity
@Table(schema = "shared", name = "audit")
@Getter
@Setter
@NoArgsConstructor
public class AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Integer id;

    // Who performed the action (null = the system)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "affected_table", nullable = false, length = 50)
    private String affectedTable;

    @Column(name = "affected_record_id")
    private Integer affectedRecordId;

    @Column(name = "date", nullable = false, columnDefinition = "datetime")
    private LocalDateTime date;

    @Column(name = "detail", columnDefinition = "nvarchar(max)")
    private String detail;
}
