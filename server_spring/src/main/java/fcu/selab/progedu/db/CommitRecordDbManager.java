package fcu.selab.progedu.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fcu.selab.progedu.data.CommitRecord;
import fcu.selab.progedu.status.StatusEnum;
import fcu.selab.progedu.utils.ExceptionUtil;

public class CommitRecordDbManager {
  UserDbManager userDbManager = UserDbManager.getInstance();
  private static final String FIELD_NAME_STATUS = "status";
  private static CommitRecordDbManager dbManager = new CommitRecordDbManager();
  private static CommitStatusDbManager csDb = CommitStatusDbManager.getInstance();

  public static CommitRecordDbManager getInstance() {
    return dbManager;
  }

  private IDatabase database = MySqlDatabase.getInstance();

  private static final Logger LOGGER = LoggerFactory.getLogger(CommitRecordDbManager.class);

  /**
   * insert student commit records into db
   * 
   * @param auId         auId
   * @param commitNumber commitNumber
   * @param status       status Id
   * @param time         commit time
   */
  public void insertCommitRecord(int auId, int commitNumber, StatusEnum status, Date time) {
    String sql = "INSERT INTO Commit_Record" + "(auId, commitNumber, status, time) "
        + "VALUES(?, ?, ?, ?)";
    int statusId = csDb.getStatusIdByName(status.getType());
    Timestamp date = new Timestamp(time.getTime());

    Connection conn = null;
    PreparedStatement preStmt = null;

    try {
      conn = database.getConnection();
      preStmt = conn.prepareStatement(sql);

      preStmt.setInt(1, auId);
      preStmt.setInt(2, commitNumber);
      preStmt.setInt(3, statusId);
      preStmt.setTimestamp(4, date);
      preStmt.executeUpdate();
    } catch (SQLException e) {
      LOGGER.debug(ExceptionUtil.getErrorInfoFromException(e));
      LOGGER.error(e.getMessage());
    } finally {
      CloseDBUtil.closeAll(preStmt, conn);
    }
  }

  /**
   * get each hw's CommitRecordId
   *
   * @param auId         Commit_Record auId
   * @param commitNumber commit number
   * @return id
   */
  public int getCommitRecordId(int auId, int commitNumber) {
    String query = "SELECT id FROM Commit_Record where auId = ? and commitNumber = ?";
    int id = 0;

    Connection conn = null;
    PreparedStatement preStmt = null;
    ResultSet rs = null;

    try {

      conn = database.getConnection();
      preStmt = conn.prepareStatement(query);

      preStmt.setInt(1, auId);
      preStmt.setInt(2, commitNumber);

      rs = preStmt.executeQuery();
      if (rs.next()) {
        id = rs.getInt("id");
      }

    } catch (SQLException e) {
      LOGGER.debug(ExceptionUtil.getErrorInfoFromException(e));
      LOGGER.error(e.getMessage());
    } finally {
      CloseDBUtil.closeAll(rs, preStmt, conn);
    }

    return id;
  }

  /**
   * get all hw's CommitRecordId by auid
   *
   * @param auId Commit_Record auId
   * @return lsCRid list of CRid
   */
  public List<Integer> getCommitRecordId(int auId) {
    String query = "SELECT id FROM Commit_Record where auId = ?";
    List<Integer> lsCRid = new ArrayList<>();

    Connection conn = null;
    PreparedStatement preStmt = null;
    ResultSet rs = null;

    try {
      conn = database.getConnection();
      preStmt = conn.prepareStatement(query);

      preStmt.setInt(1, auId);

      rs = preStmt.executeQuery();
      while (rs.next()) {
        int id = rs.getInt("id");
        lsCRid.add(id);
      }

    } catch (SQLException e) {
      LOGGER.debug(ExceptionUtil.getErrorInfoFromException(e));
      LOGGER.error(e.getMessage());
    } finally {
      CloseDBUtil.closeAll(rs, preStmt, conn);
    }

    return lsCRid;
  }

  /**
   * get each hw's CommitRecordStateCounts
   *
   * @param auId Commit_Record auId
   * @param num  num
   * @return status
   */
  public int getCommitRecordStatus(int auId, int num) {
    int status = -1;
    String query = "SELECT status FROM Commit_Record where auId = ? and commitNumber = ?";


    Connection conn = null;
    PreparedStatement preStmt = null;
    ResultSet rs = null;

    try {

      conn = database.getConnection();
      preStmt = conn.prepareStatement(query);

      preStmt.setInt(1, auId);
      preStmt.setInt(2, num);

      rs = preStmt.executeQuery();
      if (rs.next()) {
        status = rs.getInt(FIELD_NAME_STATUS);
      }

    } catch (SQLException e) {
      LOGGER.debug(ExceptionUtil.getErrorInfoFromException(e));
      LOGGER.error(e.getMessage());
    } finally {
      CloseDBUtil.closeAll(rs, preStmt, conn);
    }

    return status;
  }

  /**
   * get commit record details from the homework of a student
   * 
   * 
   * @param auId auId
   * @return commit record details
   */
  public List<CommitRecord> getCommitRecord(int auId) {
    String sql = "SELECT * FROM Commit_Record WHERE auId=?";
    List<CommitRecord> commitRecords = new ArrayList<>();

    Connection conn = null;
    PreparedStatement preStmt = null;
    ResultSet rs = null;

    try {

      conn = database.getConnection();
      preStmt = conn.prepareStatement(sql);

      preStmt.setInt(1, auId);

      rs = preStmt.executeQuery();
      while (rs.next()) {
        int statusId = rs.getInt("status");
        StatusEnum statusEnum = csDb.getStatusNameById(statusId);
        int commitNumber = rs.getInt("commitNumber");
        Date commitTime = rs.getTimestamp("time");
        CommitRecord commitRecord = new CommitRecord();
        commitRecord.setNumber(commitNumber);
        commitRecord.setStatus(statusEnum);
        commitRecord.setTime(commitTime);
        commitRecords.add(commitRecord);
      }

    } catch (SQLException e) {
      LOGGER.debug(ExceptionUtil.getErrorInfoFromException(e));
      LOGGER.error(e.getMessage());
    } finally {
      CloseDBUtil.closeAll(rs, preStmt, conn);
    }

    return commitRecords;
  }

  /**
   * get last commit record details from assigned homework of one student
   * 
   * 
   * @param auId auId
   * @return last commit record details
   */
  public JSONObject getLastCommitRecord(int auId) {
    String sql = "SELECT * from Commit_Record as a WHERE (a.commitNumber = "
        + "(SELECT max(commitNumber) FROM Commit_Record WHERE auId = ?) AND auId = ?);";
    JSONObject ob = new JSONObject();

    Connection conn = null;
    PreparedStatement preStmt = null;
    ResultSet rs = null;


    try {

      conn = database.getConnection();
      preStmt = conn.prepareStatement(sql);

      preStmt.setInt(1, auId);
      preStmt.setInt(2, auId);

      rs = preStmt.executeQuery();
      while (rs.next()) {
        int statusId = rs.getInt("status");
        StatusEnum statusEnum = csDb.getStatusNameById(statusId);
        int commitNumber = rs.getInt("commitNumber");
        Date commitTime = rs.getTimestamp("time");
        ob.put("status", statusEnum.getType());
        ob.put("commitNumber", commitNumber);
        ob.put("commitTime", commitTime);
      }

    } catch (SQLException e) {
      LOGGER.debug(ExceptionUtil.getErrorInfoFromException(e));
      LOGGER.error(e.getMessage());
    } finally {
      CloseDBUtil.closeAll(rs, preStmt, conn);
    }

    return ob;
  }

  /**
   * get commit count by auId
   * 
   * 
   * @param auid auId
   * @return aId assignment Id
   */
  public int getCommitCount(int auid) {
    int commitNumber = 0;
    String sql = "SELECT commitNumber from Commit_Record a where (a.commitNumber = "
        + "(SELECT max(commitNumber) FROM Commit_Record WHERE auId = ?));";

    Connection conn = null;
    PreparedStatement preStmt = null;
    ResultSet rs = null;


    try {

      conn = database.getConnection();
      preStmt = conn.prepareStatement(sql);

      preStmt.setInt(1, auid);
      rs = preStmt.executeQuery();
      while (rs.next()) {
        commitNumber = rs.getInt("commitNumber");
      }

    } catch (SQLException e) {
      LOGGER.debug(ExceptionUtil.getErrorInfoFromException(e));
      LOGGER.error(e.getMessage());
    } finally {
      CloseDBUtil.closeAll(rs, preStmt, conn);
    }

    return commitNumber;
  }

  /**
   * delete built record of specific auId
   *
   * @param auId auId
   */
  public void deleteRecord(int auId) {
    String sql = "DELETE FROM Commit_Record WHERE auId=?";

    Connection conn = null;
    PreparedStatement preStmt = null;

    try {

      conn = database.getConnection();
      preStmt = conn.prepareStatement(sql);

      preStmt.setInt(1, auId);
      preStmt.executeUpdate();
    } catch (SQLException e) {
      LOGGER.debug(ExceptionUtil.getErrorInfoFromException(e));
      LOGGER.error(e.getMessage());
    } finally {
      CloseDBUtil.closeAll(preStmt, conn);
    }
  }
  
  /**
   * get a part of commit record details from the homework of a student
   * 
   * 
   * @param auId auId
   * @param currentPage current page
   * @return commit record details
   */
  public List<CommitRecord> getPartCommitRecord(int auId, int currentPage) {
    String sql = "SELECT * FROM ProgEdu.Commit_Record WHERE auId = ? "
        + "AND commitNumber IN (SELECT commitNumber FROM "
        + "ProgEdu.Commit_Record WHERE commitNumber BETWEEN ? AND ?)";
    List<CommitRecord> commitRecords = new ArrayList<>();
    int totalCommitNumber = getCommitCount(auId);
    int startSearchNumber = totalCommitNumber - (currentPage - 1) * 5;
    int endSearchNumber = startSearchNumber - 4;
    if (endSearchNumber <= 0) {
      endSearchNumber = 1;
    }

    Connection conn = null;
    PreparedStatement preStmt = null;
    ResultSet rs = null;


    try {

      conn = database.getConnection();
      preStmt = conn.prepareStatement(sql);

      preStmt.setInt(1, auId);
      preStmt.setInt(2, endSearchNumber);
      preStmt.setInt(3, startSearchNumber);

      rs = preStmt.executeQuery();
      while (rs.next()) {
        int statusId = rs.getInt("status");
        StatusEnum statusEnum = csDb.getStatusNameById(statusId);
        int commitNumber = rs.getInt("commitNumber");
        Date commitTime = rs.getTimestamp("time");
        CommitRecord commitRecord = new CommitRecord();
        commitRecord.setNumber(commitNumber);
        commitRecord.setStatus(statusEnum);
        commitRecord.setTime(commitTime);
        commitRecords.add(commitRecord);
      }

    } catch (SQLException e) {
      LOGGER.debug(ExceptionUtil.getErrorInfoFromException(e));
      LOGGER.error(e.getMessage());
    } finally {
      CloseDBUtil.closeAll(preStmt, conn);
    }
    return commitRecords;
  }
}

