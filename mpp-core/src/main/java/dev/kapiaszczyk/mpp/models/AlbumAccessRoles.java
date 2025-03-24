package dev.kapiaszczyk.mpp.models;

/**
 * Represents roles that can be assigned to users in an album.
 */
public enum AlbumAccessRoles {

    /**
     * Role that allows only viewing photos in the album
     */
    VIEWER,

    /**
     * Role that allows viewing, adding and deleting photos in the album
     */
    EDITOR,

    /**
     * Role that allows viewing, adding, deleting photos and managing permissions in tthe album
     */
    ADMINISTRATOR;

    public static AlbumAccessRoles fromString(String role) {
        return switch (role) {
            case "VIEWER" -> VIEWER;
            case "EDITOR" -> EDITOR;
            case "ADMINISTRATOR" -> ADMINISTRATOR;
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }

    public static String toString(AlbumAccessRoles role) {
        return switch (role) {
            case VIEWER -> "VIEWER";
            case EDITOR -> "EDITOR";
            case ADMINISTRATOR -> "ADMINISTRATOR";
        };
    }

}
