package org.pdf.takeyourtime.configuration;

import org.pdf.takeyourtime.models.Department;
import org.pdf.takeyourtime.models.User;
import org.pdf.takeyourtime.services.DepartmentService;
import org.pdf.takeyourtime.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DBConfig implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(DBConfig.class);

  @Value("${default.admin.username}")
  private String defaultAdminUsername;

  @Value("${default.admin.password}")
  private String defaultAdminPassword;

  @Value("${default.departmentname}")
  private String defaultDepartmentName;

  private static final Long DEFAULT_ID = 1L;

  private final UserService userService;
  private final DepartmentService departmentService;

  public DBConfig(UserService userService, DepartmentService departmentService) {
    this.userService = userService;
    this.departmentService = departmentService;
  }

  @Override
  public void run(String... args) {
    if ((userService.getUserById(DEFAULT_ID).isEmpty() && userService.getAmountAdmins() <= 0) || departmentService.getDepartmentById(DEFAULT_ID).isEmpty()) {
      try {
        Department defaultDepartment = departmentService.addEmptyDepartment(defaultDepartmentName);
        
        if (defaultDepartment == null) {
          throw new RuntimeException("Default department could not be created");
        }
  
        LOGGER.info("Default department created successfully");
        User defaultUser = userService.createDefaultUser(defaultDepartment.getId(), defaultAdminUsername, defaultAdminPassword);
        
        if (defaultUser == null) {
          throw new RuntimeException("Default user could not be created");
        }
  
        LOGGER.info("Default user created successfully");
      } catch(Exception e) {
        LOGGER.error("Defaults could not be created", e);
      }
    }
  }
}