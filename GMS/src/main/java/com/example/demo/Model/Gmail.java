
package com.example.demo.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Gmail {

    private String id;
    private String sender;
    private String subject;
    private String date;
    private String body;
}
