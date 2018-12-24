package com.lmx.pactdemoprovider;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api")
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
        Map root = new HashMap();
        Map map = new HashMap();
        map.put("token", demoReq.getName());
        map.put("lastLoginIp", "harry");
        map.put("lastLoginTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        root.put("code", 1);
        root.put("message", "ok");
        root.put("data", map);
        return new ResponseEntity(root, HttpStatus.OK);
    }

    @Data
    public static class DemoReq {
        private String name, pwd;
        private Long expire;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date loginTime;
        private InnerReq inner;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InnerReq {
        private String tel;
        private String email;
    }
}
