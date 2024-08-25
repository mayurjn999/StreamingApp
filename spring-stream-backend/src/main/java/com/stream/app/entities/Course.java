package com.stream.app.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "yt_courses")
public class Course {

    @Id
    private  String id;

    private  String title;

//    @OneToMany(mappedBy = "course")
//    private List<Video> list=new ArrayList<>();
}
