package com.example.bookvoyager;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import java.util.List;

public class SampleMockitoTest {

    @Test
    public void testListMock() {
        List<String> mockedList = mock(List.class);
        when(mockedList.get(0)).thenReturn("Hello");
        assert mockedList.get(0).equals("Hello");
    }
}
