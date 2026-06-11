package com.focusnode.service;

import com.focusnode.model.Note;
import com.focusnode.model.Task;
import com.focusnode.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SqliteAppDataServiceTest {

    @Mock
    private TaskRepository taskRepo;

    @Mock
    private NoteRepository noteRepo;

    @InjectMocks
    private SqliteAppDataService service;

    @Test
    public void testGetTasks() {
        Task mockTask = mock(Task.class);
        when(mockTask.getTitle()).thenReturn("Test Task");
        when(taskRepo.findAll()).thenReturn(Arrays.asList(mockTask));

        List<Task> tasks = service.getTasks();

        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
        verify(taskRepo, times(1)).findAll();
    }

    @Test
    public void testSaveTask_NewTask() {
        Task mockTask = mock(Task.class);
        when(mockTask.getId()).thenReturn(0);

        service.saveTask(mockTask);

        verify(taskRepo, times(1)).add(mockTask);
        verify(taskRepo, never()).update(any(Task.class));
    }

    @Test
    public void testSaveTask_ExistingTask() {
        Task mockTask = mock(Task.class);
        when(mockTask.getId()).thenReturn(1);

        service.saveTask(mockTask);

        verify(taskRepo, times(1)).update(mockTask);
        verify(taskRepo, never()).add(any(Task.class));
    }

    @Test
    public void testGetNotes() {
        Note mockNote = mock(Note.class);
        when(mockNote.getTitle()).thenReturn("Test Note");
        when(noteRepo.findAll()).thenReturn(Arrays.asList(mockNote));

        List<Note> notes = service.getNotes();

        assertEquals(1, notes.size());
        assertEquals("Test Note", notes.get(0).getTitle());
        verify(noteRepo, times(1)).findAll();
    }
}
