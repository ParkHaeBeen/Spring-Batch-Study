package com.example.springbatchstudy.batch;

import com.example.springbatchstudy.entity.Member;
import com.example.springbatchstudy.entity.Team;
import com.example.springbatchstudy.entity.TeamRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformManager;
  private final EntityManagerFactory entityManagerFactory;

  @PersistenceContext
  private EntityManager manager;
  private int chunkSize=10;

  @Bean
  public Job memberJob(){
    return new JobBuilder("memberJob",jobRepository)
        .start(memberStep())
        .build();
  }

  @Bean
  @JobScope
  public Step memberStep() {
    return new StepBuilder("reserveStep",jobRepository)
        .<Team, Team>chunk(chunkSize,platformManager)
        .reader(teamReader())
        .processor(teamMemberProcessor())
        .writer(memberWriter())
        .build();
  }

  @Bean
  public JpaPagingItemReader <Team> teamReader(){

    return new JpaPagingItemReaderBuilder <Team>()
        .name("teamReader")
        .entityManagerFactory(entityManagerFactory)
        .pageSize(chunkSize)
        .queryString("SELECT t FROM Team t ")
        .build();
  }

  @Bean
  public ItemProcessor<Team,Team> teamMemberProcessor(){
    return member ->{
      List <Member> members = member.getMembers();
      for (Member member1 : members) {
        member1.winUp();
      }
      System.out.println(member);
      return member;
    };
  }

  @Bean
  public JpaItemWriter <Team> memberWriter(){
    return new JpaItemWriterBuilder <Team>()
        .entityManagerFactory(entityManagerFactory)
        .build();
  }
}
