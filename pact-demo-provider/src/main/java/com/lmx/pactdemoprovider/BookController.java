package com.lmx.pactdemoprovider;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class BookController {

    @PostMapping("/book/list")
    ResponseEntity<List<Book>> list(@RequestBody BookType bookType) throws Exception {
        List<Book> list = new ArrayList<>();
        Book book = new Book();
//        book.setAuthor("johnx");
        book.setAuthor("john");
        list.add(book);
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @PostMapping("/pact")
    ResponseEntity<Map> pact(@RequestBody DemoReq demoReq) {
        Map map = new HashMap();
        map.put("hello", demoReq.getName());
//        map.put("hello", "harry1");
        return new ResponseEntity(map, HttpStatus.OK);
    }

    public static class DemoReq{
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
