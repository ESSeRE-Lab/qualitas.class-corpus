-- Fix bug 1586532, assume no data (there is a FK issue with R_Issueknown)
ALTER TABLE R_ISSUEKNOWN DROP CONSTRAINT RISSUEREC_RISSUEKNOWN;

ALTER TABLE R_IssueRecommendation DROP CONSTRAINT R_IssueRecommendation_KEY;
ALTER TABLE R_IssueRecommendation DROP COLUMN R_IssueRecommendation_ID;
ALTER TABLE R_IssueRecommendation ADD (R_IssueRecommendation_ID NUMBER(10) NOT NULL);
ALTER TABLE R_IssueRecommendation ADD CONSTRAINT R_IssueRecommendation_KEY PRIMARY KEY (R_IssueRecommendation_ID);

-- fix FK issue with R_Issueknown
ALTER TABLE R_Issueknown DROP COLUMN R_IssueRecommendation_ID;
ALTER TABLE R_Issueknown ADD (R_IssueRecommendation_ID NUMBER(10));
ALTER TABLE R_ISSUEKNOWN ADD CONSTRAINT RISSUEREC_RISSUEKNOWN FOREIGN KEY (R_ISSUERECOMMENDATION_ID) REFERENCES R_ISSUERECOMMENDATION (R_ISSUERECOMMENDATION_ID);

