package com.btec.fpt.campus_expense_manager.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import com.btec.fpt.campus_expense_manager.entities.Transaction;
import com.btec.fpt.campus_expense_manager.entities.User;
import com.btec.fpt.campus_expense_manager.entities.Category;
import com.btec.fpt.campus_expense_manager.models.BalanceInfor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseDB";
    private static final int DATABASE_VERSION = 5;

    // Transactions table
    private static final String TABLE_TRANSACTION = "transactions";
    private static final String COLUMN_TRANSACTION_ID = "id";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_CATEGORY = "category";


    // User table
    private static final String TABLE_USER = "USER";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_FIRST_NAME = "firstName";
    private static final String COLUMN_LAST_NAME = "lastName";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    // Category table
    private static final String TABLE_CATEGORY = "CATEGORY";
    private static final String COLUMN_CATEGORY_ID = "category_id";
    private static final String COLUMN_CATEGORY_NAME = "name";

    public  DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create transactions table
        String CREATE_TRANSACTION_TABLE = "CREATE TABLE " + TABLE_TRANSACTION + "("
                + COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_AMOUNT + " REAL, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_DATE + " TEXT,"
                + COLUMN_TYPE + " INTEGER,"
                + COLUMN_EMAIL + " TEXT,"
                + COLUMN_CATEGORY + " TEXT" +

                ")";
        db.execSQL(CREATE_TRANSACTION_TABLE);

        // Create user table
//        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
//                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
//                + COLUMN_FIRST_NAME + " TEXT, "
//                + COLUMN_LAST_NAME + " TEXT, "
//                + COLUMN_EMAIL + " TEXT UNIQUE, "
//                + COLUMN_PASSWORD + " TEXT" + ")";
//        db.execSQL(CREATE_USER_TABLE);

        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + " ("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FIRST_NAME + " TEXT, "
                + COLUMN_LAST_NAME + " TEXT, "
                + COLUMN_EMAIL + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USER_TABLE);


        // Create category table
        String CREATE_CATEGORY_TABLE = "CREATE TABLE " + TABLE_CATEGORY + "("
                + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CATEGORY_NAME + " TEXT UNIQUE NOT NULL,"
                + COLUMN_EMAIL + " TEXT "+

        ")";
        db.execSQL(CREATE_CATEGORY_TABLE);

        insertDefaultCategories(db, null);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        onCreate(db);
    }

    // Insert a new transaction record
    public boolean insertTransaction(double amount, String description,
                                     String date, int type, String email, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_CATEGORY, category);

        long result = db.insert(TABLE_TRANSACTION, null, values);
        db.close();
        return result != -1;
    }

    public boolean updateTransaction(int id, double amount, String description,
                                     String date, int type, String email, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_CATEGORY, category);


        int rowsAffected = db.update(TABLE_TRANSACTION, values, COLUMN_TRANSACTION_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_TRANSACTION, COLUMN_TRANSACTION_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsDeleted > 0;
    }

    // Phương thức xóa tất cả giao dịch
    public void clearAllTransactions() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM transactions"); // "transactions" là tên bảng của bạn, thay đổi nếu cần
        db.close();
    }

    public ArrayList<Transaction> getFilteredTransactions(String startDate, String endDate, String category) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_TRANSACTION + " WHERE 1=1";
        ArrayList<String> args = new ArrayList<>();

        if (!startDate.isEmpty()) {
            query += " AND " + COLUMN_DATE + " >= ?";
            args.add(startDate);
        }
        if (!endDate.isEmpty()) {
            query += " AND " + COLUMN_DATE + " <= ?";
            args.add(endDate);
        }
        if (!category.isEmpty()) {
            query += " AND " + COLUMN_CATEGORY + " = ?";
            args.add(category);
        }

        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
                );
                transactions.add(transaction);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return transactions;
    }


    // Insert a new user record
    public boolean insertUser(String firstName, String lastName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_EMAIL, email);

        String hashPassword = hashPassword(password);
        values.put(COLUMN_PASSWORD, hashPassword);

        long result = db.insert(TABLE_USER, null, values);
        db.close();
        return result != -1;
    }


    public boolean updateUser(int userId, String firstName, String lastName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_EMAIL, email);
        String hashPassword = hashPassword(password);

        values.put(COLUMN_PASSWORD, hashPassword);

        int rowsAffected = db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_USER, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsDeleted > 0;
    }

    // Insert a new category record
//    public boolean insertCategory(String name, String email) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_CATEGORY_NAME, name);
//        values.put(COLUMN_EMAIL, email);
//
//        long result = db.insert(TABLE_CATEGORY, null, values);
//        db.close();
//        return result != -1;
//    }

    public boolean insertCategory(String email, String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the category already exists for the given email
        String query = "SELECT * FROM " + TABLE_CATEGORY +
                " WHERE " + COLUMN_CATEGORY_NAME + " = ? AND " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{categoryName, email});

        if (cursor.getCount() > 0) {
            // Category already exists
            cursor.close();
            db.close();
            return false; // Indicate failure to add category
        }

        // Insert the new category
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, categoryName);
        values.put(COLUMN_EMAIL, email);

        long result = db.insert(TABLE_CATEGORY, null, values);
        cursor.close();
        db.close();

        return result != -1; // Return true if insertion succeeded, false otherwise
    }



    public boolean updateCategory(int categoryId, String name, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, name);
        values.put(COLUMN_EMAIL, email);

        int rowsAffected = db.update(TABLE_CATEGORY, values, COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_CATEGORY, COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        db.close();
        return rowsDeleted > 0;
    }


    // Retrieve all categories as a List<Category>
    public List<Category> getCategoryList() {
        List<Category> categoryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CATEGORY, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_CATEGORY_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY_NAME));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));

                Category category = new Category(id, name, email);
                categoryList.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categoryList;
    }


    public List<Category> getAllCategoryByEmail(String email) {
        List<Category> categoryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Use "IS NULL" in SQL query when email is null
        String query;
        String[] queryParams;

        if (email == null) {
            query = "SELECT * FROM " + TABLE_CATEGORY + " WHERE " + COLUMN_EMAIL + " IS NULL";
            queryParams = null;
        } else {
            query = "SELECT * FROM " + TABLE_CATEGORY + " WHERE " + COLUMN_EMAIL + " = ? OR " + COLUMN_EMAIL + " IS NULL";
            queryParams = new String[]{email};
        }

        Cursor cursor = db.rawQuery(query, queryParams);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_CATEGORY_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY_NAME));

                Category category = new Category(id, name, email);
                categoryList.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categoryList;
    }

    // Retrieve all transactions
    public List<Transaction> getTransactionList() {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTION, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_TRANSACTION_ID));
                @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                @SuppressLint("Range") int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
                @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));

                Transaction transaction = new Transaction(id, amount, description, date, type, email, category);
                transactionList.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transactionList;
    }


    // Retrieve all transactions for a user by email
    public List<Transaction> getAllTransactionsByEmail(String email) {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_TRANSACTION + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_TRANSACTION_ID));
                @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                @SuppressLint("Range") int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
                @SuppressLint("Range") String email2 = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
                @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));

                Transaction transaction = new Transaction(id, amount, description, date,type, email2, category );
                transactionList.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transactionList;
    }

    // Function to get a user by email
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?", new String[]{email});

        if (cursor.moveToFirst()) {
            // Extract user details from the cursor
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
            @SuppressLint("Range") String firstName = cursor.getString(cursor.getColumnIndex(COLUMN_FIRST_NAME));
            @SuppressLint("Range") String lastName = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_NAME));
            @SuppressLint("Range") String userEmail = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
            @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));

            // Create a User object with the retrieved data
            user = new User(id, firstName, lastName, userEmail, password);
        }

        cursor.close();
        db.close();
        return user;
    }

    // Retrieve all users
    public List<User> getUserList() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
                @SuppressLint("Range") String firstName = cursor.getString(cursor.getColumnIndex(COLUMN_FIRST_NAME));
                @SuppressLint("Range") String lastName = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_NAME));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
                @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));

                User user = new User(id, firstName, lastName, email, password);
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return userList;
    }


    public BalanceInfor getBalanceFromEmail(String email){

        User userFound = getUserByEmail(email);
        if(userFound!=null){

            String firstName = userFound.getFirstName();
            String lastName = userFound.getLastName();

            List<Transaction> allTransaction = getAllTransactionsByEmail(email);

            double expense = 0;
            double income = 0;
            double balance = 0;
            for(Transaction transaction: allTransaction){

                if(transaction.getType()==0){
                    expense += transaction.getAmount();
                }else if(transaction.getType()==1){
                    income += transaction.getAmount();
                }
            }

            balance = income - expense;

            BalanceInfor balanceInfor = new BalanceInfor();
            balanceInfor.setBalance(balance);
            balanceInfor.setFirstName(firstName);
            balanceInfor.setLastName(lastName);

            return  balanceInfor;

        }


        return  null;
    }


    // Method to insert default categories into the database
    private void insertDefaultCategories(SQLiteDatabase db, String email) {
        String[] defaultCategories = {"Food", "Transport", "Entertainment", "Utilities", "Health","House Fee","Shopping", "Salary"};

        for (String categoryName : defaultCategories) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CATEGORY_NAME, categoryName);
            values.put(COLUMN_EMAIL, email); // Set email or leave it null for default
            db.insert(TABLE_CATEGORY, null, values);
        }
    }

    // Helper method to hash the password
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Sign Up function
    public boolean signUp(String firstName, String lastName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?", new String[]{email});

        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return false; // User already exists
        }

        String hashedPassword = hashPassword(password); // Hash the password
        if (hashedPassword == null) {
            cursor.close();
            db.close();
            return false; // Hashing failed
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashedPassword);

        long result = db.insert(TABLE_USER, null, values);
        cursor.close();
        db.close();
        return result != -1;
    }

    // Sign In function
    public boolean signIn(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String hashedPassword = hashPassword(password); // Hash the password for comparison
        if (hashedPassword == null) {
            db.close();
            return false; // Hashing failed
        }

        String query = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, hashedPassword});

        boolean isAuthenticated = cursor.getCount() > 0;
        cursor.close();
        db.close();

        return isAuthenticated;
    }


    public boolean changePassword(String email, String oldPassword, String newPassword) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get the current password for the given email
        String query = "SELECT " + COLUMN_PASSWORD + " FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            String storedHashedPassword = cursor.getString(0);
            cursor.close();

            // Check if the old password matches
            String hashedOldPassword = hashPassword(oldPassword);
            if (storedHashedPassword.equals(hashedOldPassword)) {
                db = this.getWritableDatabase();
                ContentValues values = new ContentValues();

                // Hash the new password before updating
                String hashedNewPassword = hashPassword(newPassword);
                values.put(COLUMN_PASSWORD, hashedNewPassword);

                // Update the password
                int rowsAffected = db.update(TABLE_USER, values, COLUMN_EMAIL + " = ?", new String[]{email});
                db.close();

                return rowsAffected > 0; // Return true if the update was successful
            } else {
                cursor.close();
                return false; // Old password does not match
            }
        }

        if (cursor != null) cursor.close();
        db.close();
        return false; // User not found or error
    }

    public HashMap<String, Double> getSpendingByCategory(String email) {
        HashMap<String, Double> spending = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT category, SUM(amount) AS total FROM transactions WHERE email = ? GROUP BY category";

        Cursor cursor = db.rawQuery(query, new String[]{email});
        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(0);
                double total = cursor.getDouble(1);
                spending.put(category, total);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return spending;
    }

    public HashMap<String, Double> getCategoryFluctuations(String email) {
        HashMap<String, Double> fluctuations = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT category, MAX(amount) - MIN(amount) AS fluctuation FROM transactions WHERE email = ? GROUP BY category";

        Cursor cursor = db.rawQuery(query, new String[]{email});
        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(0);
                double fluctuation = cursor.getDouble(1);
                fluctuations.put(category, fluctuation);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return fluctuations;
    }


}
