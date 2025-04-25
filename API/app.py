from datetime import datetime
from flask import Flask, request, jsonify
import sqlite3

app = Flask(__name__)
DATABASE = "EvidencijaRadnogVremena.db"

# Function to connect to the database
def get_db_connection():
    conn = sqlite3.connect(DATABASE)
    conn.row_factory = sqlite3.Row  # Enables column access by name
    return conn

def create_tables():
    with get_db_connection() as conn:
        cursor = conn.cursor()
        
        # Create Users Table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS Users (
                Id INTEGER PRIMARY KEY AUTOINCREMENT,
                Username TEXT UNIQUE NOT NULL,
                Password TEXT NOT NULL
            )
        ''')
        
        # Create WorkTime Table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS WorkTime (
                Id INTEGER PRIMARY KEY AUTOINCREMENT,
                UserId INTEGER NOT NULL,
                StartTime TEXT NOT NULL,
                EndTime TEXT,
                Description TEXT,
                FOREIGN KEY (UserId) REFERENCES Users(Id) ON DELETE CASCADE
            )
        ''')

        # Insert default users if the table is empty
        cursor.execute("SELECT COUNT(*) FROM Users")
        if cursor.fetchone()[0] == 0:
            default_users = [
                ("admin", "admin123"),
                ("user1", "password1"),
                ("user2", "password2")
            ]
            cursor.executemany("INSERT INTO Users (Username, Password) VALUES (?, ?)", default_users)

        conn.commit()

# Call the function to create tables
create_tables()

@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({"message": "Hello World"}), 200

@app.route("/worktime", methods=["POST"])
def add_worktime():
    data = request.get_json()
    user_id = data.get("UserId")
    start_time = data.get("StartTime")
    end_time = data.get("EndTime")
    description = data.get("Description")

    if not user_id or not start_time:
        return jsonify({"error": "UserId and StartTime are required"}), 400

    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute("INSERT INTO WorkTime (UserId, StartTime, EndTime, Description) VALUES (?, ?, ?, ?)", 
                       (user_id, start_time, end_time, description))
        conn.commit()

    return jsonify({"message": "WorkTime entry added!"}), 201

@app.route("/worktime/<int:user_id>", methods=["GET"])
def get_worktime_by_user(user_id):
    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM WorkTime WHERE UserId = ?", (user_id,))
        work_entries = cursor.fetchall()

    work_list = [{"Id": row["Id"], "UserId": row["UserId"], "StartTime": row["StartTime"], 
                  "EndTime": row["EndTime"], "Description": row["Description"]} for row in work_entries]
    
    return jsonify(work_list)

@app.route("/users", methods=["GET"])
def get_users():
    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute("SELECT Id, Username FROM Users")  # Do NOT return passwords for security reasons
        users = cursor.fetchall()

    users_list = [{"Id": row["Id"], "Username": row["Username"]} for row in users]
    return jsonify(users_list)

@app.route("/users/<int:user_id>", methods=["GET"])
def get_user_by_id(user_id):
    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute("SELECT Id, Username FROM Users WHERE Id = ?", (user_id,))
        user = cursor.fetchone()

    if user is None:
        return jsonify({"error": "User not found"}), 404

    user_data = {"Id": user["Id"], "Username": user["Username"]}
    return jsonify(user_data)

@app.route("/login", methods=["POST"])
def login():
    data = request.get_json()
    username = data.get("Username")
    password = data.get("Password")

    if not username or not password:
        return jsonify({"error": "Username and Password are required"}), 400

    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM Users WHERE Username = ? AND Password = ?", (username, password))
        user = cursor.fetchone()

    if user is None:
        return jsonify({"error": "Invalid username or password"}), 401

    return jsonify({"message": "Login successful", "UserId": user["Id"]}), 200

@app.route("/start_work", methods=["POST"])
def start_work():
    data = request.get_json()
    user_id = data.get("UserId")

    if not user_id:
        return jsonify({"error": "UserId is required"}), 400

    start_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO WorkTime (UserId, StartTime) VALUES (?, ?)",
            (user_id, start_time),
        )
        conn.commit()
    
    return jsonify({"message": "Work started", "StartTime": start_time}), 201

@app.route("/end_work", methods=["PUT"])
def end_work():
    data = request.get_json()
    user_id = data.get("UserId")
    description = data.get("Description")
    end_time_millis = data.get("EndTime") # Get EndTime in milliseconds

    if not user_id or end_time_millis is None:
        return jsonify({"error": "UserId and EndTime are required"}), 400

    # Convert milliseconds to datetime object
    end_time_dt = datetime.fromtimestamp(end_time_millis / 1000)  # Convert ms to seconds
    end_time_str = end_time_dt.strftime("%Y-%m-%d %H:%M:%S")

    conn = get_db_connection()
    if conn is None:
        return jsonify({"error": "Failed to connect to database"}), 500

    cursor = conn.cursor()
    cursor.execute(
        "UPDATE WorkTime SET EndTime = ?, Description = ? WHERE UserId = ? AND EndTime IS NULL",
        (end_time_str, description, user_id),
    )
    conn.commit()
    conn.close()

    if cursor.rowcount == 0:
        return jsonify({"error": "No active work session found"}), 404

    return jsonify({"message": "Work ended", "EndTime": end_time_str}), 200

@app.route("/work_history", methods=["GET"])
def work_history():
    user_id = request.args.get("UserId")

    if not user_id:
        return jsonify({"error": "UserId is required"}), 400

    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute(
            "SELECT Id, StartTime, EndTime, Description, DATE(StartTime) as Date FROM WorkTime WHERE UserId = ? ORDER BY StartTime DESC",
            (user_id,),
        )
        rows = cursor.fetchall()

        history = [
            {
                "Id": row["Id"],
               "Date": row["Date"],
                "StartTime": row["StartTime"],
                 "EndTime": row["EndTime"] if row["EndTime"] else "Ongoing",
                "Description": row["Description"] if row["Description"] else "No description"
            }
            for row in rows
        ]

    return jsonify(history), 200

@app.route('/register', methods=['POST'])
def register():
    data = request.get_json()

    if not data:
        return jsonify({'message': 'No input data provided'}), 400

    print("Received data:", data)  # Debug print

    username = data.get('username')
    password = data.get('password')

    if not username or not password:
        return jsonify({'message': 'Username and password are required'}), 400

    # Check if user exists
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM Users WHERE username = ?", (username,))
    if cursor.fetchone():
        conn.close()
        return jsonify({"message": "Username already exists"}), 409

    # Insert new user
    cursor.execute("INSERT INTO Users (username, password) VALUES (?, ?)", (username, password))
    conn.commit()
    conn.close()
    return jsonify({"message": "Registration successful"}), 200

@app.route('/delete_work_history', methods=['POST'])
def delete_work_history():
    data = request.get_json()
    if not data or 'id' not in data:
        return jsonify({'error': 'Missing "id" parameter'}), 400

    entry_id = data['id']

    try:
        with get_db_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM WorkTime WHERE Id = ?", (entry_id,))
            conn.commit()
            if cursor.rowcount > 0:
                return jsonify({'message': 'Work history entry deleted successfully.'}), 200
            else:
                return jsonify({'message': 'No work history entry found with the provided ID.'}), 404
    except sqlite3.Error as e:
        return jsonify({'error': str(e)}), 500


if __name__ == "__main__":
    app.run(debug=True)