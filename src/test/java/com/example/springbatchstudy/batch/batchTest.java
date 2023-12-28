package com.example.springbatchstudy.batch;

import com.example.springbatchstudy.entity.Member;
import com.example.springbatchstudy.entity.MemberRepository;
import com.example.springbatchstudy.entity.Team;
import com.example.springbatchstudy.entity.TeamRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBatchTest
@SpringBootTest
public class batchTest {

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private TeamRepository teamRepository;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private JobConfiguration jobConfig;

  @Test
  @Rollback(value = false)
  @Transactional
  void test(){
    //given
    Team team= Team.builder()
        .build();

    for(int i=0;i<100;i++){
      Member member= Member.builder()
          .name("member"+i)
          .build();
      team.addMember(member);
    }

    for (Member member : team.getMembers()) {
      System.out.println(member);
    }

    teamRepository.save(team);
    entityManager.flush();
    //when

    Team team1= teamRepository.findById(team.getId()).get();

    //then
    System.out.println("---");
    for (Member member : team1.getMembers()) {
      System.out.println(member.getName());
    }
  }

  @Test
  @Rollback(value = false)
  void test2() throws Exception {
    //given
    Team team= Team.builder()
        .build();

    for(int i=0;i<100;i++){
      Member member= Member.builder()
          .name("member"+i)
          .team(team)
          .build();
      team.addMember(member);
    }

    for (Member member : team.getMembers()) {
      System.out.println(member);
    }

    Team team1 = teamRepository.saveAndFlush(team);
    for (Member member : team1.getMembers()) {
      System.out.println(member);
    }

    //when

    System.out.println("--------------");
    jobLauncherTestUtils.setJob(jobConfig.memberJob());
    JobExecution jobExecution = jobLauncherTestUtils.launchJob();

    //then


  }


}
