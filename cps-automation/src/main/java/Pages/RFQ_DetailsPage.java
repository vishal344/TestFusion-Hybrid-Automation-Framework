package Pages;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.*;
import java.time.Duration;
import java.util.*;

public class RFQ_DetailsPage {

	WebDriver driver;
	WebDriverWait wait;
	WebDriverWait longWait;
	WebDriverWait shortWait;
	WebDriverWait alertWait;

	public RFQ_DetailsPage(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		this.longWait = new WebDriverWait(driver, Duration.ofSeconds(60)); // FIX: increased from 30s to 60s
		this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
		this.alertWait = new WebDriverWait(driver, Duration.ofMillis(500));
	}

	// ── Locators ──────────────────────────────────────────────────
	By procurementMenu = By.xpath("//span[contains(text(),'Procurement')]");
	By rfqMenu         = By.xpath("//span[contains(text(),'RFQ')]");
	By rfqid           = By.xpath("//a[contains(@onclick,'redirectToRFQ')]");
	By addRowBtn       = By.id("addRowBtn");
	By sendEmailBtn    = By.id("sendMail");

	private Map<String, Integer> tdMap   = new LinkedHashMap<>();

	private Map<Integer, String> imageMap = new LinkedHashMap<>();

	private static final int MAX_ATTEMPTS = 3;

	// ─────────────────────────────────────────────────────────────
	// HELPERS
	// ─────────────────────────────────────────────────────────────

	private void sleep(long ms) {
		try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
	}

	private void dismissAlertIfPresent() {
		try {
			Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());
			System.out.println("  [Alert] Dismissing: " + alert.getText());
			alert.accept();
		} catch (Exception ignored) {}
	}

	private void dismissToastrIfPresent() {
		try {
			String toastrSelector = "div.toastr-message, div.toast, div.toast-message, "
					+ "div.toastr, div#toast-container, div.jq-toast-wrap";

			try {
				new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
					List<WebElement> toasts = d.findElements(By.cssSelector(toastrSelector));
					return toasts.stream().noneMatch(WebElement::isDisplayed);
				});
				return;
			} catch (TimeoutException ignored) {}

			((JavascriptExecutor) driver).executeScript(
					"var selectors = ['.toastr-message','.toast','.toast-message',"
					+ "'.toastr','#toast-container','.jq-toast-wrap'];"
					+ "selectors.forEach(function(sel){"
					+ "  document.querySelectorAll(sel).forEach(function(el){"
					+ "    el.parentNode && el.parentNode.removeChild(el);"
					+ "  });"
					+ "});"
					+ "var body = document.querySelector('body');"
					+ "if(body) body.style.pointerEvents = '';");

			System.out.println("  [Toastr] Force-removed blocking notification");
			sleep(150);

		} catch (Exception e) {
			System.out.println("  [Toastr] Dismissal attempt failed (non-fatal): " + e.getMessage());
		}
	}

	private void safeSendKeys(By locator, String value) {
		for (int i = 0; i < MAX_ATTEMPTS; i++) {
			try {
				dismissAlertIfPresent();
				dismissToastrIfPresent();

				WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);

				try {
					el.click();
				} catch (ElementClickInterceptedException intercepted) {
					System.out.println("  [ClickIntercepted] Retrying with JS click after toastr check");
					dismissToastrIfPresent();
					sleep(300);
					el = driver.findElement(locator);
					((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
				}

				el.clear();
				el.sendKeys(value);
				((JavascriptExecutor) driver).executeScript(
						"arguments[0].dispatchEvent(new Event('input',{bubbles:true}));"
						+ "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", el);
				return;
			} catch (StaleElementReferenceException e) {
				System.out.println("  [StaleRetry] safeSendKeys attempt " + (i + 1) + " for: " + locator);
				sleep(300);
			}
		}
		throw new RuntimeException("safeSendKeys failed after " + MAX_ATTEMPTS + " attempts: " + locator);
	}

	private void safeJsSetValue(By locator, String value) {
		for (int i = 0; i < MAX_ATTEMPTS; i++) {
			try {
				WebElement el = driver.findElement(locator);
				((JavascriptExecutor) driver).executeScript(
						"arguments[0].value=arguments[1];"
						+ "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));"
						+ "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", el, value);
				return;
			} catch (StaleElementReferenceException e) {
				System.out.println("  [StaleRetry] safeJsSetValue attempt " + (i + 1) + " for: " + locator);
				sleep(300);
			}
		}
		throw new RuntimeException("safeJsSetValue failed after " + MAX_ATTEMPTS + " attempts: " + locator);
	}

	private void waitForDom() {
		dismissAlertIfPresent();
		wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
	}

	// ─────────────────────────────────────────────────────────────
	// NAVIGATE
	// ─────────────────────────────────────────────────────────────

	public void navigateToRFQPage() {
		wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

		WebElement procurement = wait.until(ExpectedConditions.elementToBeClickable(procurementMenu));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", procurement);
		try {
			procurement.click();
		} catch (Exception e) {
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", procurement);
		}

		wait.until(ExpectedConditions.elementToBeClickable(rfqMenu)).click();

		wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
		sleep(1500);

		String rfqIdToSearch = System.getProperty("rfqId", "NT79");
		System.out.println("[Navigation] Searching for RFQ ID: " + rfqIdToSearch);

		try {
			WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("search2")));
			searchBox.clear();
			searchBox.sendKeys(rfqIdToSearch);
			sleep(1500);
			System.out.println("[Navigation] Search text entered: " + rfqIdToSearch);
		} catch (Exception e) {
			System.out.println("[Navigation] Search box not found: " + e.getMessage());
		}

		boolean clicked = false;

		try {
			By rfqLinkBy = By.xpath("//a[contains(@onclick,'" + rfqIdToSearch + "')]");
			WebElement rfqLink = new WebDriverWait(driver, Duration.ofSeconds(10))
					.until(ExpectedConditions.elementToBeClickable(rfqLinkBy));
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", rfqLink);
			System.out.println("[Navigation] Clicked RFQ link: " + rfqIdToSearch);
			clicked = true;
		} catch (Exception e) {
			System.out.println("[Navigation] Try 1 failed: " + e.getMessage());
		}

		if (!clicked) {
			try {
				By rfqLinkBy = By.xpath("//a[contains(@class,'rfq-id')]");
				WebElement rfqLink = new WebDriverWait(driver, Duration.ofSeconds(10))
						.until(ExpectedConditions.elementToBeClickable(rfqLinkBy));
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", rfqLink);
				System.out.println("[Navigation] Clicked RFQ link by class (Try 2)");
				clicked = true;
			} catch (Exception e) {
				System.out.println("[Navigation] Try 2 failed: " + e.getMessage());
			}
		}

		if (!clicked) {
			throw new RuntimeException("Could not find and click RFQ link for ID: " + rfqIdToSearch);
		}

		wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
		sleep(1000);
		System.out.println("[Navigation] RFQ details page loaded");
	}

	// ─────────────────────────────────────────────────────────────
	// ADD ROW
	// ─────────────────────────────────────────────────────────────

	public void clickAddRow() {
		dismissAlertIfPresent();
		dismissToastrIfPresent();

		int rowsBefore = driver.findElements(By.xpath("//table//tbody//tr[td]")).size();

		WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(addRowBtn));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
		((JavascriptExecutor) driver).executeScript("window.scrollBy(0,-100);");
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

		long deadline = System.currentTimeMillis() + 30000;
		while (System.currentTimeMillis() < deadline) {
			int currentRows = driver.findElements(By.xpath("//table//tbody//tr[td]")).size();
			if (currentRows == rowsBefore + 1) break;
			if (currentRows > rowsBefore + 1) {
				System.out.println("[WARN] More than 1 row added — stopping");
				break;
			}
			sleep(300);
		}

		try {
			new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> d
					.findElements(By.xpath(
							"//table//tbody//tr[last()]//input | //table//tbody//tr[last()]//textarea"))
					.size() >= 1);
		} catch (TimeoutException ignored) {
			System.out.println("  [WARN] New row fields slow to appear");
		}

		sleep(300);
		System.out.println("New row added. Total rows: "
				+ driver.findElements(By.xpath("//table//tbody//tr[td]")).size());
	}

	// ─────────────────────────────────────────────────────────────
	// DISCOVER TD INDICES
	// ─────────────────────────────────────────────────────────────

	public void discoverTdIndices() {
		System.out.println("=== Auto-discovering TD indices from table header ===");
		tdMap.clear();

		List<WebElement> headers = driver.findElements(By.xpath("//table//thead//tr[last()]//th"));
		if (headers.isEmpty())
			headers = driver.findElements(By.xpath("//table//thead//tr[last()]//td"));

		for (int i = 0; i < headers.size(); i++) {
			String text = headers.get(i).getText().trim().replaceAll("\\s+", " ").toLowerCase();
			int tdIdx = i + 1;

			if (text.contains("part") && (text.contains("number") || text.contains("no") || text.contains("#")))
				tdMap.put("partNumber", tdIdx);
			else if ((text.contains("part") && text.contains("name")) || text.equals("description"))
				tdMap.put("partName", tdIdx);
			else if (text.contains("resin") || text.contains("material"))
				tdMap.put("resin", tdIdx);
			else if (text.contains("cav"))
				tdMap.put("cav", tdIdx);
			else if (text.contains("injection") || text.contains("inj") || text.contains("runner"))
				tdMap.put("injSystem", tdIdx);
			else if (text.contains("gate"))
				tdMap.put("gate", tdIdx);
			else if (text.contains("eau") || text.contains("annual") || text.contains("volume"))
				tdMap.put("eau", tdIdx);
			else if (text.contains("steel"))
				tdMap.put("steel", tdIdx);
			else if (text.contains("ejection") || text.contains("eject"))
				tdMap.put("ejection", tdIdx);
			else if ((text.contains("mold") && text.contains("feat")) || text.contains("feature"))
				tdMap.put("moldFeatures", tdIdx);
			else if (text.contains("target") && text.contains("price"))
				tdMap.put("targetPrice", tdIdx);
			else if (text.contains("note") || text.contains("remark") || text.contains("comment"))
				tdMap.put("notes", tdIdx);
		}

		System.out.println("Discovered TD map: " + tdMap);
		if (tdMap.size() < 5) {
			System.out.println("WARNING: Header discovery insufficient. Falling back to positional.");
			discoverTdIndicesByPosition();
		}
	}

	private void discoverTdIndicesByPosition() {
		System.out.println("=== Positional TD discovery (fallback) ===");
		tdMap.clear();

		List<WebElement> tds = driver.findElements(By.xpath("//table//tbody//tr[last()]//td"));

		int leadingSkip = 0;
		for (WebElement td : tds) {
			boolean useful = !td.findElements(By.tagName("input")).isEmpty()
					|| !td.findElements(By.tagName("textarea")).isEmpty()
					|| !td.findElements(By.tagName("select")).isEmpty()
					|| !td.findElements(By.xpath(".//span[contains(@class,'price')]")).isEmpty();
			if (!useful) leadingSkip++;
			else break;
		}
		System.out.println("  Leading non-input TDs: " + leadingSkip);

		leadingSkip = 2;
		System.out.println("  Forcing leadingSkip=2 for consistent TD mapping");

		String[] colOrder = { "partNumber", "partName", null, "resin", "cav", "injSystem", "gate",
				"eau", "steel", "ejection", "moldFeatures", "targetPrice", null, null, "notes" };

		int colIdx = 0;
		for (int i = leadingSkip; i < tds.size() && colIdx < colOrder.length; i++) {
			int tdIdx = i + 1;
			String colName = colOrder[colIdx];
			if (colName != null) {
				tdMap.put(colName, tdIdx);
				System.out.println("  td[" + tdIdx + "] → " + colName);
			} else {
				System.out.println("  td[" + tdIdx + "] → (skip)");
			}
			colIdx++;
		}
		System.out.println("Positional TD map: " + tdMap);
	}

	// ─────────────────────────────────────────────────────────────
	// ENTER TEXT
	// ─────────────────────────────────────────────────────────────

	private void enterText(int tdIndex, String value) {
		if (value == null || value.trim().isEmpty()) {
			System.out.println("  Skipped td[" + tdIndex + "] — empty");
			return;
		}

		String base = "//table//tbody//tr[last()]//td[" + tdIndex + "]";
		By specificTextareaBy = By.xpath(base + "//textarea[contains(@class,'rfq-textarea-display')]");
		By anyTextareaBy      = By.xpath(base + "//textarea[not(contains(@style,'display:none')) and not(contains(@style,'display: none'))]");
		By specificInputBy    = By.xpath(base + "//input[contains(@class,'editable-field') and not(contains(@class,'rfq-hidden-input'))]");
		By anyInputBy         = By.xpath(base + "//input[not(@type='hidden') and not(@type='file')]");
		By selectBy           = By.xpath(base + "//select");
		By contentEditableBy  = By.xpath(base + "//*[@contenteditable='true']");
		By hiddenInputBy      = By.xpath(base + "//input[contains(@class,'rfq-hidden-input')]");

		boolean hasSpecificTextarea = !driver.findElements(specificTextareaBy).isEmpty();
		boolean hasAnyTextarea      = !driver.findElements(anyTextareaBy).isEmpty();
		boolean hasSpecificInput    = !driver.findElements(specificInputBy).isEmpty();
		boolean hasAnyInput         = !driver.findElements(anyInputBy).isEmpty();
		boolean hasSelect           = !driver.findElements(selectBy).isEmpty();
		boolean hasContentEditable  = !driver.findElements(contentEditableBy).isEmpty();

		if (hasSpecificTextarea || hasAnyTextarea) {
			By taBy = hasSpecificTextarea ? specificTextareaBy : anyTextareaBy;
			safeSendKeys(taBy, value);
			if (!driver.findElements(hiddenInputBy).isEmpty())
				safeJsSetValue(hiddenInputBy, value);
			System.out.println("  td[" + tdIndex + "] textarea ← " + value);
			return;
		}
		if (hasSpecificInput || hasAnyInput) {
			By inpBy = hasSpecificInput ? specificInputBy : anyInputBy;
			safeSendKeys(inpBy, value);
			System.out.println("  td[" + tdIndex + "] input ← " + value);
			return;
		}
		if (hasSelect) {
			for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
				try {
					new Select(wait.until(ExpectedConditions.elementToBeClickable(selectBy)))
							.selectByVisibleText(value);
					System.out.println("  td[" + tdIndex + "] select ← " + value);
					return;
				} catch (StaleElementReferenceException e) {
					sleep(300);
				}
			}
		}
		if (hasContentEditable) {
			safeSendKeys(contentEditableBy, value);
			System.out.println("  td[" + tdIndex + "] contenteditable ← " + value);
			return;
		}
		By tdBy = By.xpath("(//table//tbody//tr[last()]//td)[" + tdIndex + "]");
		for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
			try {
				WebElement td = driver.findElement(tdBy);
				((JavascriptExecutor) driver).executeScript(
						"var td=arguments[0],v=arguments[1];"
						+ "var el=td.querySelector('input')||td.querySelector('textarea');"
						+ "if(el){el.value=v;"
						+ "el.dispatchEvent(new Event('input',{bubbles:true}));"
						+ "el.dispatchEvent(new Event('change',{bubbles:true}));}"
						+ "else{td.innerText=v;}",
						td, value);
				System.out.println("  td[" + tdIndex + "] JS-inject ← " + value);
				return;
			} catch (StaleElementReferenceException e) {
				sleep(300);
			}
		}
		System.out.println("  Skipped td[" + tdIndex + "] — could not fill");
	}

	// ─────────────────────────────────────────────────────────────
	// UPLOAD IMAGE
	// ─────────────────────────────────────────────────────────────

	public void uploadImageForRow(String imagePath) {
		if (imagePath == null || imagePath.isEmpty()) {
			System.out.println("  [Image] No image path — skipping");
			return;
		}
		File imgFile = new File(imagePath);
		if (!imgFile.exists()) {
			System.out.println("  [Image] File not found: " + imagePath + " — skipping");
			return;
		}

		String actualDataLine = null;
		for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
			try {
				WebElement priceSpan = wait.until(ExpectedConditions.presenceOfElementLocated(
						By.xpath("//table//tbody//tr[last()]//span[contains(@class,'price-input')]")));
				actualDataLine = priceSpan.getAttribute("data-line");
				if (actualDataLine != null && !actualDataLine.isEmpty()) break;
			} catch (StaleElementReferenceException e) {
				System.out.println("  [Image] Stale on data-line read, retry " + (attempt + 1));
				sleep(400);
			} catch (Exception e) {
				System.out.println("  [Image] Could not read data-line: " + e.getMessage());
				break;
			}
		}

		if (actualDataLine == null || actualDataLine.isEmpty()) {
			System.out.println("  [Image] data-line unknown — skipping upload");
			return;
		}

		System.out.println("  [Image] Uploading for data-line=" + actualDataLine + " | file=" + imgFile.getName());

		By uploadLinkBy = By.xpath("//a[contains(@class,'upload-link') and @data-line='" + actualDataLine + "']");
		List<WebElement> uploadLinks = driver.findElements(uploadLinkBy);
		if (uploadLinks.isEmpty()) {
			System.out.println("  [Image] Upload link not found for data-line=" + actualDataLine + " — skipping");
			return;
		}

		WebElement uploadLink = uploadLinks.get(0);
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", uploadLink);
		sleep(200);

		try {
			byte[] imgBytes = java.nio.file.Files.readAllBytes(imgFile.toPath());
			String base64Image = java.util.Base64.getEncoder().encodeToString(imgBytes);
			String mimeType = "image/png";
			String lname = imgFile.getName().toLowerCase();
			if (lname.endsWith(".jpg") || lname.endsWith(".jpeg")) mimeType = "image/jpeg";
			else if (lname.endsWith(".gif"))  mimeType = "image/gif";
			else if (lname.endsWith(".webp")) mimeType = "image/webp";

			String jsIntercept = "(function() {"
					+ "  var origClick = HTMLInputElement.prototype.click;"
					+ "  HTMLInputElement.prototype.click = function() {"
					+ "    if (this.type === 'file') {"
					+ "      var b64='" + base64Image + "', mime='" + mimeType + "', fname='" + imgFile.getName() + "';"
					+ "      try {"
					+ "        var bin=atob(b64), arr=new Uint8Array(bin.length);"
					+ "        for(var i=0;i<bin.length;i++) arr[i]=bin.charCodeAt(i);"
					+ "        var blob=new Blob([arr],{type:mime});"
					+ "        var file=new File([blob],fname,{type:mime,lastModified:Date.now()});"
					+ "        var dt=new DataTransfer(); dt.items.add(file);"
					+ "        this.files=dt.files;"
					+ "        var self=this;"
					+ "        setTimeout(function(){"
					+ "          self.dispatchEvent(new Event('change',{bubbles:true}));"
					+ "          self.dispatchEvent(new Event('input',{bubbles:true}));"
					+ "        },100);"
					+ "      } catch(e){console.error('intercept error:',e);}"
					+ "      HTMLInputElement.prototype.click=origClick;"
					+ "      return;"
					+ "    }"
					+ "    return origClick.apply(this,arguments);"
					+ "  };"
					+ "})();";

			((JavascriptExecutor) driver).executeScript(jsIntercept);
			System.out.println("  [Image] File input interceptor installed");
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", uploadLink);
			System.out.println("  [Image] Upload link clicked (intercepted)");
			sleep(800);
			dismissAlertIfPresent();
			System.out.println("  [Image] Upload complete for data-line=" + actualDataLine);

		} catch (Exception e) {
			System.out.println("  [Image] Intercept failed: " + e.getMessage() + " — trying Robot fallback");
			try {
				uploadLink.click();
				sleep(1500);
				Robot robot = new Robot();
				StringSelection sel = new StringSelection(imgFile.getAbsolutePath());
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
				sleep(200);
				robot.keyPress(KeyEvent.VK_CONTROL);
				robot.keyPress(KeyEvent.VK_A);
				robot.keyRelease(KeyEvent.VK_A);
				robot.keyRelease(KeyEvent.VK_CONTROL);
				sleep(100);
				robot.keyPress(KeyEvent.VK_CONTROL);
				robot.keyPress(KeyEvent.VK_V);
				robot.keyRelease(KeyEvent.VK_V);
				robot.keyRelease(KeyEvent.VK_CONTROL);
				sleep(200);
				robot.keyPress(KeyEvent.VK_ENTER);
				robot.keyRelease(KeyEvent.VK_ENTER);
				sleep(1500);
				dismissAlertIfPresent();
				System.out.println("  [Image] Robot fallback upload done for data-line=" + actualDataLine);
			} catch (Exception robotEx) {
				System.out.println("  [Image] Robot fallback failed: " + robotEx.getMessage());
			}
		}
	}

	// ─────────────────────────────────────────────────────────────
	// TARGET PRICE POPUP
	// FIX A: Wait for modal inputs to be fully INTERACTABLE (not just visible)
	// before filling fields. On Jenkins the modal opens but inputs are still
	// animating — filling them immediately causes "element not interactable".
	// ─────────────────────────────────────────────────────────────

	public void handleTargetPricePopup(String rawPrice) {
		String price = rawPrice.replaceAll("[^0-9.]", "").trim();
		if (price.isEmpty()) price = "0";
		System.out.println("  Opening Target Price popup for value: " + price);

		dismissToastrIfPresent();

		By modalBy     = By.id("customTargetPopupModal");
		By priceSpanBy = By.xpath("//table//tbody//tr[last()]//span[contains(@class,'price-input')]");

		// Re-fetch price span fresh — never use a cached reference across DOM mutations
		WebElement priceSpan = null;
		for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
			try {
				priceSpan = wait.until(ExpectedConditions.elementToBeClickable(priceSpanBy));
				break;
			} catch (StaleElementReferenceException e) {
				System.out.println("  [StaleRetry] priceSpan fetch attempt " + (attempt + 1));
				sleep(400);
			}
		}
		if (priceSpan == null) throw new RuntimeException("Could not get price span after retries");

		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", priceSpan);
		sleep(200);

		System.out.println("  Price span: class='" + priceSpan.getAttribute("class")
				+ "' data-line='" + priceSpan.getAttribute("data-line") + "'");

		new Actions(driver).doubleClick(priceSpan).perform();
		System.out.println("  Double-clicked price span");
		sleep(600);

		for (int attempt = 2; attempt <= 3; attempt++) {
			boolean open = false;
			try {
				List<WebElement> m = driver.findElements(modalBy);
				open = !m.isEmpty() && m.get(0).isDisplayed();
			} catch (Exception ignored) {}
			if (open) break;
			System.out.println("  Modal not open, JS dblclick attempt " + attempt);
			dismissToastrIfPresent();
			WebElement freshSpan = driver.findElement(priceSpanBy);
			((JavascriptExecutor) driver).executeScript(
					"arguments[0].dispatchEvent(new MouseEvent('dblclick',"
					+ "{bubbles:true,cancelable:true,view:window}));", freshSpan);
			sleep(600);
		}

		boolean modalOpen = false;
		try {
			List<WebElement> m = driver.findElements(modalBy);
			modalOpen = !m.isEmpty() && m.get(0).isDisplayed();
		} catch (Exception ignored) {}
		if (!modalOpen) throw new RuntimeException("Target price modal did not open.");
		System.out.println("  Modal is open");

		WebElement modal = driver.findElement(modalBy);

		// FIX A: Wait for inputs to be present AND clickable before interacting.
		// On slow Jenkins machines the modal animates open — inputs exist in DOM
		// but are not yet interactable. This caused "element not interactable"
		// for Margin Factor, Shipping Factor, Tariff Factor, CPS Handling Cost on Row 9.
		By marginFactorInputBy = By.xpath(
				"//div[@id='customTargetPopupModal']"
				+ "//label[contains(text(),'Margin Factor')]"
				+ "/following::input[not(@type='hidden')][1]");
		try {
			new WebDriverWait(driver, Duration.ofSeconds(15))
					.until(ExpectedConditions.elementToBeClickable(marginFactorInputBy));
			System.out.println("  Modal fully loaded — inputs interactable");
		} catch (TimeoutException e) {
			System.out.println("  [WARN] Modal inputs slow — proceeding anyway");
			sleep(1000); // last-resort wait
		}

		// Re-fetch modal after waiting (DOM may have updated during animation)
		modal = driver.findElement(modalBy);
		List<WebElement> modalInputs = modal.findElements(By.xpath(".//input[not(@type='hidden')]"));
		System.out.println("  Modal inputs: " + modalInputs.size());

		clearAndType(modalInputs.get(0), price);
		System.out.println("  Target Price Set <- " + price);
		sleep(150);

		fillModalField(modal, "Margin Factor",    "10");  sleep(100);
		fillModalField(modal, "Shipping Factor",  "5");   sleep(100);
		fillModalField(modal, "Tariff Factor",    "12");  sleep(100);
		fillModalField(modal, "CPS Handling Cost","720"); sleep(150);

		List<WebElement> allBtns = modal.findElements(By.tagName("button"));
		System.out.println("  Buttons in modal: " + allBtns.size());
		WebElement saveBtn = null;
		for (WebElement btn : allBtns) {
			String cls = btn.getAttribute("class") != null ? btn.getAttribute("class").toLowerCase() : "";
			String txt = btn.getText().trim();
			System.out.println("    btn text='" + txt + "' class='" + cls + "'");
			boolean isClose = cls.contains("close") || txt.equalsIgnoreCase("x")
					|| txt.equalsIgnoreCase("cancel") || txt.equalsIgnoreCase("close");
			if (!isClose && saveBtn == null) saveBtn = btn;
		}
		if (saveBtn == null && !allBtns.isEmpty()) saveBtn = allBtns.get(allBtns.size() - 1);
		if (saveBtn == null) throw new RuntimeException("No Save button in target price modal.");

		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", saveBtn);
		sleep(100);
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);
		System.out.println("  Save clicked: '" + saveBtn.getText() + "'");

		// Poll for modal close
		boolean closed = false;
		long deadline = System.currentTimeMillis() + 30_000;
		while (System.currentTimeMillis() < deadline) {
			dismissAlertIfPresent();
			try {
				List<WebElement> m = driver.findElements(modalBy);
				if (m.isEmpty() || !m.get(0).isDisplayed()) {
					closed = true;
					break;
				}
			} catch (Exception e) {
				closed = true;
				break;
			}
			sleep(300);
		}

		if (closed) {
			System.out.println("  Modal closed");
		} else {
			System.out.println("  Modal still open after 30s — forcing close");
			try {
				((JavascriptExecutor) driver).executeScript(
						"var m=document.getElementById('customTargetPopupModal');"
						+ "if(m){m.style.display='none';"
						+ "document.body.classList.remove('modal-open');"
						+ "var b=document.querySelector('.modal-backdrop');if(b)b.remove();}");
			} catch (Exception ignored) {}
			dismissAlertIfPresent();

			try {
				new WebDriverWait(driver, Duration.ofSeconds(5)).until(d ->
						d.findElements(By.cssSelector(".modal-backdrop")).isEmpty()
				);
				System.out.println("  Backdrop cleared");
			} catch (TimeoutException ignored) {
				try {
					((JavascriptExecutor) driver).executeScript(
							"document.querySelectorAll('.modal-backdrop').forEach(function(el){"
							+ "  el.parentNode && el.parentNode.removeChild(el);"
							+ "});"
							+ "document.body.classList.remove('modal-open');"
							+ "document.body.style.overflow='';");
					System.out.println("  Backdrop force-removed via JS");
				} catch (Exception ignored2) {}
			}
		}

		try {
			sleep(400);
			dismissToastrIfPresent();
		} catch (Exception ignored) {}

		try {
			wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
		} catch (Exception ignored) {}
		dismissAlertIfPresent();
		sleep(500);
		System.out.println("  Target Price popup done");
	}

	// ─────────────────────────────────────────────────────────────
	// SEND EMAIL BUTTON
	// ─────────────────────────────────────────────────────────────

	public void clickSendEmail() {
		System.out.println("  Clicking Send Email button...");
		dismissToastrIfPresent();
		WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(sendEmailBtn));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
		sleep(1000);
		dismissAlertIfPresent();
		System.out.println("  Send Email clicked — waiting for version popup...");

		// FIX B: Use longWait (60s) instead of wait (20s) for version popup.
		// Jenkins is slower — the popup can take 30-40s to appear after clicking Send Email.
		try {
			longWait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//button[contains(@onclick,'createVersion')]")));
			System.out.println("  Version popup is visible");
		} catch (Exception e) {
			System.out.println("  [WARN] Version popup not detected — may have opened compose directly");
		}
	}

	// ─────────────────────────────────────────────────────────────
	// CREATE VERSION POPUP
	// FIX B: Increase timeout to longWait (60s) and add multiple selector
	// fallbacks. On Jenkins the version popup appears after a slow server
	// response — 20s was not enough.
	// ─────────────────────────────────────────────────────────────

	public void handleCreateVersionPopup() {
		System.out.println("  Handling Create Version popup...");

		By createVersionBtnBy        = By.xpath("//button[contains(@onclick,'createVersion')]");
		By createVersionBtnByText    = By.xpath("//button[contains(text(),'Create Version')]");
		By createVersionBtnByClass   = By.xpath("//button[contains(@class,'create-version')]");

		boolean clicked = false;

		// Attempt 1: wait with longWait then click
		try {
			WebElement createBtn = longWait.until(ExpectedConditions.elementToBeClickable(createVersionBtnBy));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", createBtn);
			sleep(300);
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", createBtn);
			System.out.println("  'Create Version' clicked (attempt 1 - onclick)");
			clicked = true;
		} catch (Exception e) {
			System.out.println("  [WARN] Attempt 1 failed: " + e.getMessage());
		}

		// Attempt 2: try by button text
		if (!clicked) {
			try {
				WebElement createBtn = new WebDriverWait(driver, Duration.ofSeconds(10))
						.until(ExpectedConditions.elementToBeClickable(createVersionBtnByText));
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", createBtn);
				System.out.println("  'Create Version' clicked (attempt 2 - text)");
				clicked = true;
			} catch (Exception e) {
				System.out.println("  [WARN] Attempt 2 failed: " + e.getMessage());
			}
		}

		// Attempt 3: try by class
		if (!clicked) {
			try {
				WebElement createBtn = new WebDriverWait(driver, Duration.ofSeconds(10))
						.until(ExpectedConditions.elementToBeClickable(createVersionBtnByClass));
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", createBtn);
				System.out.println("  'Create Version' clicked (attempt 3 - class)");
				clicked = true;
			} catch (Exception e) {
				System.out.println("  [WARN] Attempt 3 failed: " + e.getMessage());
			}
		}

		// Attempt 4: JS direct function call
		if (!clicked) {
			System.out.println("  [WARN] All button attempts failed — calling JS createVersion()");
			((JavascriptExecutor) driver).executeScript("if(typeof createVersion==='function') createVersion();");
		}

		sleep(1500);
		dismissAlertIfPresent();

		// FIX B: Use longWait for notes popup appearance check too
		try {
			longWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("notesText")));
			System.out.println("  Notes popup appeared after Create Version");
		} catch (Exception e) {
			System.out.println("  [WARN] Notes popup not immediately visible");
		}
	}

	// ─────────────────────────────────────────────────────────────
	// NOTES POPUP
	// FIX C: Use longWait (60s) instead of wait (20s) for notesText visibility.
	// On Jenkins the notes popup appears after a slow server response following
	// createVersion() — 20s caused TimeoutException at RFQ_DetailsPage.java:760.
	// Added JS fallback if normal interaction fails.
	// ─────────────────────────────────────────────────────────────

	public void handleNotesPopup(String noteText) {
		System.out.println("  Handling Notes popup with note: '" + noteText + "'");

		WebElement notesTextarea = null;

		// FIX C: Use longWait (60s) — notes popup is slow to appear on Jenkins
		try {
			notesTextarea = longWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("notesText")));
			System.out.println("  notesText is visible");
		} catch (TimeoutException e) {
			System.out.println("  [WARN] notesText not visible after 60s — trying JS scroll fallback");
			try {
				notesTextarea = driver.findElement(By.id("notesText"));
				((JavascriptExecutor) driver).executeScript(
						"arguments[0].scrollIntoView(true); arguments[0].focus();", notesTextarea);
				sleep(500);
			} catch (Exception e2) {
				throw new RuntimeException("Notes textarea not found after all attempts: " + e2.getMessage());
			}
		}

		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", notesTextarea);
		sleep(200);

		// Clear and type note text
		try {
			notesTextarea.click();
			notesTextarea.clear();
			((JavascriptExecutor) driver).executeScript(
					"arguments[0].value=''; arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
					notesTextarea);
			notesTextarea.sendKeys(noteText);
		} catch (Exception e) {
			// JS fallback for setting value
			System.out.println("  [WARN] Normal sendKeys failed, using JS: " + e.getMessage());
			((JavascriptExecutor) driver).executeScript(
					"arguments[0].value=arguments[1];"
					+ "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));"
					+ "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
					notesTextarea, noteText);
		}

		((JavascriptExecutor) driver).executeScript(
				"arguments[0].dispatchEvent(new Event('input',{bubbles:true}));"
				+ "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", notesTextarea);
		System.out.println("  Note entered: '" + noteText + "'");
		sleep(200);

		// Click save button with fallback
		try {
			WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("saveNotes")));
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);
			System.out.println("  Notes Save clicked");
		} catch (Exception e) {
			System.out.println("  [WARN] saveNotes button not found, trying JS: " + e.getMessage());
			((JavascriptExecutor) driver).executeScript(
					"var btn=document.getElementById('saveNotes');"
					+ "if(btn) btn.click();"
					+ "else if(typeof saveNotes==='function') saveNotes();");
		}

		sleep(1000);
		dismissAlertIfPresent();

		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bccField")));
			System.out.println("  Compose mail popup appeared");
		} catch (Exception e) {
			System.out.println("  [WARN] Compose popup not immediately visible after Notes save");
		}
	}

	// ─────────────────────────────────────────────────────────────
	// SELECT VENDORS
	// ─────────────────────────────────────────────────────────────

	public void selectVendors(String[] vendors) {
		System.out.println("  Opening vendor popup...");

		try {
			WebElement bccField = wait.until(ExpectedConditions.elementToBeClickable(By.id("bccField")));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", bccField);
			sleep(200);
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", bccField);
			System.out.println("  bccField clicked");
		} catch (Exception e) {
			System.out.println("  [WARN] bccField click failed, calling JS: " + e.getMessage());
			((JavascriptExecutor) driver).executeScript("if(typeof openVendorPopup==='function') openVendorPopup();");
		}

		sleep(1000);

		try {
			wait.until(d -> d.findElements(By.xpath("//input[@type='checkbox']")).size() > 0);
			System.out.println("  Vendor popup open. Checkboxes: "
					+ driver.findElements(By.xpath("//input[@type='checkbox']")).size());
		} catch (Exception e) {
			System.out.println("  [WARN] No checkboxes found in vendor popup");
		}
		sleep(200);

		for (String vendorName : vendors) {
			String name = vendorName.trim();
			System.out.println("  Selecting vendor: '" + name + "'");

			try {
				Boolean found = (Boolean) ((JavascriptExecutor) driver).executeScript(
						"var name = arguments[0];"
						+ "var cbs = document.querySelectorAll('input[type=\"checkbox\"]');"
						+ "for (var i = 0; i < cbs.length; i++) {"
						+ "  var cb = cbs[i]; var txt = '';"
						+ "  if (cb.parentElement)     txt += cb.parentElement.innerText     || '';"
						+ "  if (cb.nextSibling)        txt += cb.nextSibling.textContent     || '';"
						+ "  if (cb.nextElementSibling) txt += cb.nextElementSibling.innerText || '';"
						+ "  var lbl = document.querySelector('label[for=\"' + cb.id + '\"]');"
						+ "  if (lbl) txt += lbl.innerText || '';"
						+ "  if (txt.replace(/\\s+/g,' ').trim().indexOf(name) >= 0) {"
						+ "    cb.scrollIntoView({block:'center'});"
						+ "    if (!cb.checked) { cb.click(); cb.dispatchEvent(new Event('change',{bubbles:true})); }"
						+ "    return true;"
						+ "  }"
						+ "}"
						+ "return false;",
						name);

				if (Boolean.TRUE.equals(found)) {
					System.out.println("  ✓ Selected vendor: '" + name + "'");
				} else {
					System.out.println("  [WARN] Vendor '" + name + "' not found in popup");
				}
			} catch (Exception e) {
				System.out.println("  [WARN] JS scan failed for '" + name + "': " + e.getMessage());
			}
			sleep(200);
		}

		System.out.println("  Vendor selection complete: " + Arrays.toString(vendors));
	}

	// ─────────────────────────────────────────────────────────────
	// CLICK VENDOR SAVE BUTTON
	// ─────────────────────────────────────────────────────────────

	public void clickVendorSaveButton() {
		System.out.println("  Clicking vendor Save button...");

		By vendorSaveBtnBy = By.xpath("//button[contains(@onclick,'saveSelectedVendors')]");

		try {
			WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(vendorSaveBtnBy));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", saveBtn);
			sleep(100);
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);
			System.out.println("  ✓ Vendor Save clicked (saveSelectedVendors)");
		} catch (Exception e) {
			System.out.println("  [WARN] Button not found, calling JS directly: " + e.getMessage());
			((JavascriptExecutor) driver)
					.executeScript("if(typeof saveSelectedVendors==='function') saveSelectedVendors();");
			System.out.println("  ✓ saveSelectedVendors() called via JS");
		}

		sleep(1000);
		dismissAlertIfPresent();
		waitForDom();
		System.out.println("  Vendor Save done");
	}

	// ─────────────────────────────────────────────────────────────
	// SEND EMAIL IN COMPOSE POPUP
	// ─────────────────────────────────────────────────────────────

	public void clickSendEmailInCompose() {
		System.out.println("  Clicking Send Email in compose popup...");

		By sendBtnBy = By.xpath("//button[contains(@onclick,'sendMail')]");

		try {
			WebElement sendBtn = wait.until(ExpectedConditions.elementToBeClickable(sendBtnBy));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", sendBtn);
			sleep(200);
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", sendBtn);
			System.out.println("  Send Email (compose) clicked");
		} catch (Exception e) {
			System.out.println("  [WARN] Send button not found, calling JS: " + e.getMessage());
			((JavascriptExecutor) driver).executeScript("if(typeof sendMail==='function') sendMail();");
		}

		sleep(1500);
		dismissAlertIfPresent();
		waitForDom();
		System.out.println("  Email send action complete");
	}

	// ─────────────────────────────────────────────────────────────
	// FILL ROW DATA
	// ─────────────────────────────────────────────────────────────

	public void fillRowData(String[] rowData, int excelRowIndex) {
		int tdCount = driver.findElements(By.xpath("//table//tbody//tr[last()]//td")).size();
		System.out.println("  TD count=" + tdCount + " | tdMap=" + tdMap);

		if (tdMap.containsKey("partNumber")) {
			fillByMap("partNumber",   rowData[0],  "Part Number");
			fillByMap("partName",     rowData[1],  "Part Name");
			fillByMap("resin",        rowData[2],  "Resin");
			fillByMap("cav",          rowData[3],  "Cav");
			fillByMap("injSystem",    rowData[4],  "Injection System");
			fillByMap("gate",         rowData[5],  "Gate");
			fillByMap("eau",          rowData[6],  "EAU");
			fillByMap("steel",        rowData[7],  "Steel");
			fillByMap("ejection",     rowData[8],  "Ejection");
			fillByMap("moldFeatures", rowData[9],  "Mold Features");
			fillByMap("notes",        rowData[12], "Notes");
		} else {
			int o = (tdCount >= 33) ? 1 : 0;
			enterText(5 + o,  rowData[0]);
			enterText(7 + o,  rowData[1]);
			enterText(6 + o,  rowData[2]);
			enterText(9 + o,  rowData[3]);
			enterText(10 + o, rowData[4]);
			enterText(11 + o, rowData[5]);
			enterText(12 + o, rowData[6]);
			enterText(13 + o, rowData[7]);
			enterText(14 + o, rowData[8]);
			enterText(15 + o, rowData[9]);
			enterText(19 + o, rowData[12]);
		}

		uploadImageForRow(imageMap.get(excelRowIndex));
		handleTargetPricePopup(rowData[10]);
	}

	public void fillRowData(String[] rowData) {
		fillRowData(rowData, -1);
	}

	private void fillByMap(String colKey, String value, String colLabel) {
		if (!tdMap.containsKey(colKey)) {
			System.out.println("  WARNING: No TD found for: " + colLabel);
			return;
		}
		System.out.println("  Filling [" + colLabel + "] at td[" + tdMap.get(colKey) + "]");
		enterText(tdMap.get(colKey), value);
	}

	// ─────────────────────────────────────────────────────────────
	// CLEAR AND TYPE
	// ─────────────────────────────────────────────────────────────

	private void clearAndType(WebElement field, String value) {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", field);
		field.click();
		field.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		field.sendKeys(Keys.DELETE);
		field.clear();
		((JavascriptExecutor) driver).executeScript(
				"arguments[0].value=''; arguments[0].dispatchEvent(new Event('input',{bubbles:true}));", field);
		field.sendKeys(value);
		((JavascriptExecutor) driver).executeScript(
				"arguments[0].dispatchEvent(new Event('input',{bubbles:true}));"
				+ "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", field);
	}

	private void fillModalField(WebElement modal, String labelText, String value) {
		try {
			// Re-fetch modal element to avoid stale reference
			WebElement freshModal = driver.findElement(By.id("customTargetPopupModal"));
			WebElement field = freshModal.findElement(By.xpath(
					".//label[contains(text(),'" + labelText + "')]"
					+ "/following::input[not(@type='hidden')][1]"));
			// FIX A: Ensure field is interactable before filling
			new WebDriverWait(driver, Duration.ofSeconds(10))
					.until(ExpectedConditions.elementToBeClickable(field));
			clearAndType(field, value);
			System.out.println("  " + labelText + " ← " + value);
		} catch (Exception e) {
			System.out.println("  WARNING: Could not fill [" + labelText + "]: " + e.getMessage());
		}
	}

	// ─────────────────────────────────────────────────────────────
	// READ EXCEL (text + embedded images)
	// ─────────────────────────────────────────────────────────────

	public String[][] readExcelData(String filePath) throws Exception {
		File file = new File(filePath);
		System.out.println("Reading Excel: " + file.getAbsolutePath());

		FileInputStream fis       = new FileInputStream(file);
		XSSFWorkbook   workbook   = new XSSFWorkbook(fis);
		XSSFSheet      sheet      = workbook.getSheetAt(0);
		DataFormatter  formatter  = new DataFormatter();

		int      rowCount = sheet.getPhysicalNumberOfRows();
		int      dataCols = 13;
		String[][] data   = new String[rowCount - 1][dataCols];

		for (int i = 1; i < rowCount; i++) {
			Row row = sheet.getRow(i);
			for (int j = 0; j < dataCols; j++) {
				int  excelCol = (j < 2) ? (j + 1) : (j + 2);
				Cell cell     = (row == null) ? null : row.getCell(excelCol);
				data[i - 1][j] = (cell == null) ? "" : formatter.formatCellValue(cell);
			}
		}

		imageMap.clear();
		String tempDir = System.getProperty("java.io.tmpdir") + File.separator + "rfq_images";
		new File(tempDir).mkdirs();

		try {
			XSSFDrawing drawing = sheet.getDrawingPatriarch();
			if (drawing != null) {

				TreeMap<Integer, byte[]>  anchorToBytes = new TreeMap<>();
				TreeMap<Integer, String>  anchorToExt   = new TreeMap<>();

				for (XSSFShape shape : drawing.getShapes()) {
					if (shape instanceof XSSFPicture) {
						XSSFPicture      pic    = (XSSFPicture) shape;
						XSSFClientAnchor anchor = (XSSFClientAnchor) pic.getAnchor();
						int              aRow   = anchor.getRow1();
						XSSFPictureData  pd     = pic.getPictureData();
						anchorToBytes.put(aRow, pd.getData());
						anchorToExt.put(aRow,   pd.suggestFileExtension());
					}
				}

				int dataRowNum = 1;
				for (Map.Entry<Integer, byte[]> entry : anchorToBytes.entrySet()) {
					int    anchorRow = entry.getKey();
					byte[] imgBytes  = entry.getValue();
					String ext       = anchorToExt.get(anchorRow);
					String imgPath   = tempDir + File.separator + "part_image_row" + dataRowNum + "." + ext;

					try (FileOutputStream fos = new FileOutputStream(imgPath)) {
						fos.write(imgBytes);
					}

					imageMap.put(dataRowNum, imgPath);
					System.out.println("  Extracted image: excelRow=" + anchorRow
							+ " → dataRow=" + dataRowNum
							+ " → " + imgPath
							+ " (" + imgBytes.length + " bytes)");
					dataRowNum++;
				}
			}
		} catch (Exception e) {
			System.out.println("  [WARN] Image extraction failed: " + e.getMessage());
		}

		System.out.println("  imageMap keys: " + imageMap.keySet());
		workbook.close();
		fis.close();
		System.out.println("Total rows read: " + (rowCount - 1));
		return data;
	}

	public Map<Integer, String> getImageMap() {
		return Collections.unmodifiableMap(imageMap);
	}

	// ─────────────────────────────────────────────────────────────
	// VERIFY
	// ─────────────────────────────────────────────────────────────

	public boolean isEmailSentSuccessfully() {
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
					"//*[contains(text(),'Email sent') or "
					+ "contains(text(),'Success') or "
					+ "contains(text(),'sent successfully') or "
					+ "contains(text(),'Mail sent')]")));
			return true;
		} catch (Exception e) {
			return driver.getPageSource().contains("sent") || driver.getPageSource().contains("Success");
		}
	}
}
