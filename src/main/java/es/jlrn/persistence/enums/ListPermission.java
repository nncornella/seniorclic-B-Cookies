package es.jlrn.persistence.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor 
public enum ListPermission {
//
    USER_READ, 
    USER_CREATE,
    USER_UPDATE, 
    USER_DELETE,
    POST_READ, 
    POST_CREATE, 
    POST_UPDATE, 
    POST_DELETE,
    REPORT_VIEW, 
    REPORT_EXPORT,
    ADMIN_PANEL_ACCESS
}
