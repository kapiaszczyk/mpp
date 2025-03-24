const db_name = "mpp";
const user = "mpp";
const password = "mpp";

print("Connecting to the database: " + db_name);
db = db.getSiblingDB(db_name);

print("Creating user: " + user);
try {
    db.createUser({
        user: user,
        pwd: password,
        roles: [
            { role: "readWrite", db: db_name },
            { role: "dbAdmin", db: db_name },
        ]
    });
    print("User " + user + " created successfully.");
} catch (e) {
    print("Error creating user: " + e.message);
}


print("Verifying user creation...");
try {
    const users = Array.from(db.getUsers());
    const userExists = users.some(u => u.user === user);

    if (userExists) {
        print("User " + user + " exists in the database: " + db_name);
    } else {
        print("User " + user + " does NOT exist in the database: " + db_name);
    }
} catch (e) {
    print("Error verifying user creation: " + e.message);
}