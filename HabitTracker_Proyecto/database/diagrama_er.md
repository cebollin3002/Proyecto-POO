# Diagrama entidad relación

```mermaid
erDiagram
    HABIT ||--o{ HABIT_LOG : registra
    HABIT {
        INTEGER id PK
        TEXT name UK
        TEXT description
        TEXT created_at
    }
    HABIT_LOG {
        INTEGER id PK
        INTEGER habit_id FK
        TEXT log_date
        INTEGER completed
    }
```

**Cardinalidad:** un hábito puede tener cero o muchos registros diarios; cada registro pertenece a un solo hábito. La combinación `habit_id` + `log_date` es única para permitir como máximo un estado diario por hábito. Al eliminar un hábito se elimina su historial.
