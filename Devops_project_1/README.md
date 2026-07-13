End-to-End CI/CD Pipeline on AWS


Project Overview
This project demonstrates a complete DevOps CI/CD pipeline using Jenkins, AWS, SonarQube, Nexus Repository, Docker, and Ansible.


The pipeline automates:

•	Source Code Checkout

•	Build Automation

•	Unit Testing

•	Static Code Analysis

•	Artifact Management

•	Docker Image Creation

•	Deployment to Staging

•	Integration Testing

•	Production Deployment


The objective is to reduce manual deployment effort, improve software quality, and enable faster releases.

Architecture

         Developer
         
             |
             
             v

      GitHub Repository
      
             |
             
             v
             
      Jenkins Pipeline
      
             |
             
    +------------------+
    
    |                  |
    
    v                  v
    
SonarQube          Maven Build

(Code Quality)     Unit Tests

    |                  |
    
    +--------+---------+
    
             |
             
             v
             
      Nexus Repository
      
      (Artifact Storage)
      
             |
             
             v
             
        Docker Build
        
             |
             
             v
             
      Docker Registry
      
             |
             
             v
             
      Ansible Deploy
      
             |
             
             v
             
    Production Server

    
for more details with images check file uploaded in main branch
