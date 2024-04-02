import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.Listeners;

@Listeners(TestListener.class)
public class UserRegistrationTest {

	private WebDriver driver;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Set<String> usedEmails = new HashSet<>(); // Set to store used email addresses

    @DataProvider(name = "userData")
    public Object[][] readUserData() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("user_data.csv"));
        String line;
        int rowCount = 0;
        while ((line = reader.readLine()) != null) {
            rowCount++;
        }
        reader.close();

        reader = new BufferedReader(new FileReader("user_data.csv"));
        Object[][] data = new Object[rowCount - 1][4]; // Exclude header row
        int i = 0;
        reader.readLine(); // Skip header row
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            data[i][0] = values[0];
            data[i][1] = values[1];
            data[i][2] = values[2];
            data[i][3] = values[3];
            i++;
        }
        reader.close();
        return data;
    }

    @BeforeTest
    public void setup() throws IOException {
        // Choose your desired browser driver (comment out the other)
       
        driver = new ChromeDriver();

        // Uncomment for Firefox testing
        // System.setProperty("webdriver.gecko.driver", "path/to/geckodriver");
        // driver = new FirefoxDriver();

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();
        driver.get("https://magento.softwaretestingboard.com/");
    }

    @Test(dataProvider = "userData")
    public void registerUser(String firstName, String lastName, String baseEmail, String password) throws InterruptedException {
        this.firstName = firstName;
        this.lastName = lastName;
        
        this.password = password;
        
        email = baseEmail; // Initialize with base email from data

        // De-duplication logic
        if (usedEmails.contains(email)) {
            System.out.println("Duplicate email detected: " + email);
            email = generateUniqueEmail(baseEmail);
            System.out.println("Generated new unique email: " + email);
        }
        usedEmails.add(email);


        // Click "Sign In"
        driver.findElement(By.linkText("Sign In")).click();

        // Create Account (using explicit wait for email field)
        WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(5));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.linkText("Create an Account")).click();

        // Enter details and Register
        driver.findElement(By.id("firstname")).sendKeys(firstName);
        driver.findElement(By.id("lastname")).sendKeys(lastName);
        driver.findElement(By.id("email_address")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("password-confirmation")).sendKeys(password);
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@type='submit']")));
        driver.findElement(By.xpath("//button[@title='Create an Account']")).click();

        // Validation
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("logged-in")));
        String welcomeText = driver.findElement(By.xpath("//span[@class='account_user_name']")).getText();
        Assert.assertTrue(welcomeText.contains(firstName + " " + lastName), "Full name validation Pass");
    
        
    }
    
    private String generateUniqueEmail(String baseEmail) {
        Random random = new Random();
        int randomNumber = random.nextInt(1000); // Generate random number between 0 and 999
        return baseEmail + "+" + randomNumber + "@example.com";
    }

    @AfterTest
    public void tearDown() {
      //  driver.quit();
    }
}