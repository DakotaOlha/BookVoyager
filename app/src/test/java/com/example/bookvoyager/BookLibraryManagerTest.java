package com.example.bookvoyager;
import static org.mockito.Mockito.*;

import com.example.bookvoyager.Class.Book;
import com.example.bookvoyager.Firebase.BookLibraryManager;
import com.example.bookvoyager.Firebase.AddBookCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;

public class BookLibraryManagerTest {

    private FirebaseAuth mockAuth;
    private FirebaseUser mockUser;
    private FirebaseFirestore mockDb;
    private BookLibraryManager bookLibraryManager;

    private CollectionReference mockUserCollection;
    private DocumentReference mockDocumentReference;
    private CollectionReference mockBooksCollection;
    private Task<Void> mockTask;

    @Before
    public void setUp() {
        mockAuth = mock(FirebaseAuth.class);
        mockUser = mock(FirebaseUser.class);
        mockDb = mock(FirebaseFirestore.class);

        mockUserCollection = mock(CollectionReference.class);
        mockBooksCollection = mock(CollectionReference.class);
        mockDocumentReference = mock(DocumentReference.class);
        mockTask = mock(Task.class);

        bookLibraryManager = new BookLibraryManager(mock(Context.class)) {
            @Override
            public FirebaseAuth getAuth() {
                return mockAuth;
            }

            @Override
            public FirebaseFirestore getDb() {
                return mockDb;
            }
        };

        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("test_user_id");

        when(mockDb.collection("users")).thenReturn(mockUserCollection);
        when(mockUserCollection.document("test_user_id")).thenReturn(mockDocumentReference);
        when(mockDocumentReference.collection("books")).thenReturn(mockBooksCollection);
        when(mockBooksCollection.document()).thenReturn(mock(DocumentReference.class));
    }

    @Test
    public void testAddBookToUserLibrary_callsOnSuccess() {
        Book testBook = new Book();
        testBook.setTitle("Test Title");
        testBook.setAuthors("Test Author");
        testBook.setCoverUrl("http://test.cover");
        testBook.setPageCount(100);
        testBook.setDescription("Test Description");
        testBook.setCountry("UA");
        testBook.setISBN("1234567890");

        AddBookCallback callback = mock(AddBookCallback.class);

        DocumentReference mockBookDocRef = mock(DocumentReference.class);
        when(mockBooksCollection.document()).thenReturn(mockBookDocRef);
        when(mockBookDocRef.set(anyMap())).thenReturn(mockTask);

        when(mockTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<Void> listener = invocation.getArgument(0);
            Task<Void> successTask = mock(Task.class);
            when(successTask.isSuccessful()).thenReturn(true);
            listener.onComplete(successTask);
            return mockTask;
        });

        bookLibraryManager.addBookToUserLibrary(testBook, callback);

        verify(callback, times(1)).onSuccess();
        verify(callback, never()).onFailure(anyString());
    }
}
