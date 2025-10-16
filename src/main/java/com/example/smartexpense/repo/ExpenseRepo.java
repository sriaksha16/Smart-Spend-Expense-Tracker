package com.example.smartexpense.repo;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.smartexpense.model.Expense;
import com.example.smartexpense.model.UserReg;




@Repository
public interface ExpenseRepo extends JpaRepository<Expense, Long>  {

	  // fetch all expenses of a given user
    List<Expense> findByUser(UserReg user);
    
    
    // 2️⃣ Fetch expenses by user + type (EXPENSE / INCOME)
   List<Expense> findByUserAndType(UserReg user, String type);
   
   @Query("SELECT e FROM Expense e WHERE e.user = :user AND e.category = :category " +
	       "AND FUNCTION('MONTH', e.date) = :month AND FUNCTION('YEAR', e.date) = :year")
	List<Expense> findByUserAndCategoryAndMonthAndYear(
	        @Param("user") UserReg user,
	        @Param("category") String category,
	        @Param("month") int month,
	        @Param("year") int year
	);
   

   @Query("SELECT e.category, e.title, e.type, SUM(e.amount) " +
	       "FROM Expense e " +
	       "WHERE e.user.id = :userId " +
	       "AND FUNCTION('YEAR', e.date) = :year " +
	       "AND FUNCTION('MONTH', e.date) = :month " +
	       "GROUP BY e.category, e.title, e.type")
	List<Object[]> getCategoryTotalsDetailed(@Param("userId") Long userId,
	                                         @Param("year") int year,
	                                         @Param("month") int month);

   
   @Query("SELECT FUNCTION('MONTH', e.date), e.type, SUM(e.amount) " +
	       "FROM Expense e " +
	       "WHERE e.user.id = :userId AND FUNCTION('YEAR', e.date) = :year " +
	       "GROUP BY FUNCTION('MONTH', e.date), e.type")
	List<Object[]> getMonthlyTrends(@Param("userId") Long userId,
	                                @Param("year") int year);
	
	
	@Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
		       "WHERE e.user.id = :userId AND e.category = :category " +
		       "AND FUNCTION('MONTH', e.date) = :month " +
		       "AND FUNCTION('YEAR', e.date) = :year")
		Double getTotalSpent(@Param("userId") Long userId,
		                     @Param("category") String category,
		                     @Param("month") Integer month,
		                     @Param("year") Integer year);


    // Combine (Date + Category + Amount)
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
           "AND (:category IS NULL OR e.category = :category) " +
           "AND (:start IS NULL OR :end IS NULL OR e.date BETWEEN :start AND :end) " +
           "AND (:min IS NULL OR :max IS NULL OR e.amount BETWEEN :min AND :max)")
    List<Expense> filterExpenses(@Param("userId") Long userId,
                                 @Param("category") String category,
                                 @Param("start") LocalDate start,
                                 @Param("end") LocalDate end,
                                 @Param("min") Double min,
                                 @Param("max") Double max);
    
    @Query("SELECT e FROM Expense e WHERE LOWER(e.category) LIKE LOWER(CONCAT('%', :category, '%'))")
    List<Expense> findByCategoryIgnoreCase(@Param("category") String category);



}

    
