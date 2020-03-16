package csdev.couponstash.logic.commands;

import static csdev.couponstash.logic.commands.CommandTestUtil.assertCommandFailure;
import static csdev.couponstash.logic.commands.CommandTestUtil.assertCommandSuccess;
import static csdev.couponstash.logic.commands.CommandTestUtil.showCouponAtIndex;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import csdev.couponstash.commons.core.Messages;
import csdev.couponstash.commons.core.index.Index;
import csdev.couponstash.model.Model;
import csdev.couponstash.model.ModelManager;
import csdev.couponstash.model.UserPrefs;
import csdev.couponstash.model.coupon.Coupon;
import csdev.couponstash.model.coupon.savings.MonetaryAmount;
import csdev.couponstash.model.coupon.savings.Savings;
import csdev.couponstash.model.coupon.savings.SavingsConversionUtil;
import csdev.couponstash.testutil.CouponBuilder;
import csdev.couponstash.testutil.TypicalCoupons;
import csdev.couponstash.testutil.TypicalIndexes;

class UsedCommandTest {

    private Model model = new ModelManager(TypicalCoupons.getTypicalCouponStash(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Coupon couponToBeUsed = model.getFilteredCouponList().get(TypicalIndexes.INDEX_FIRST_COUPON.getZeroBased());
        UsedCommand usedCommand = new UsedCommand(TypicalIndexes.INDEX_FIRST_COUPON);

        ModelManager expectedModel = new ModelManager(model.getCouponStash(), new UserPrefs());
        Integer expectedUsageAmount = Integer.parseInt(couponToBeUsed.getUsage().value) + 1;
        Coupon expectedCoupon =
                new CouponBuilder(couponToBeUsed).withUsage(String.valueOf(expectedUsageAmount)).build();
        expectedModel.setCoupon(couponToBeUsed, expectedCoupon);

        String expectedMessage = String.format(UsedCommand.MESSAGE_USED_COUPON_SUCCESS, expectedCoupon.getName());

        assertCommandSuccess(usedCommand, model, expectedMessage, expectedModel);
    }

    @Test void execute_validIndexUnfilteredListWithOriginalAmount_success() {
        Coupon couponToBeUsed = model.getFilteredCouponList().get(TypicalIndexes.INDEX_SECOND_COUPON.getZeroBased());
        MonetaryAmount originalAmount = new MonetaryAmount(10.0);
        UsedCommand usedCommand = new UsedCommand(TypicalIndexes.INDEX_SECOND_COUPON, originalAmount);

        ModelManager expectedModel = new ModelManager(model.getCouponStash(), new UserPrefs());
        Integer expectedUsageAmount = Integer.parseInt(couponToBeUsed.getUsage().value) + 1;
        Savings expectedSavings = SavingsConversionUtil.convertToPure(couponToBeUsed.getSavings(), originalAmount);
        Coupon expectedCoupon =
                new CouponBuilder(couponToBeUsed).withUsage(String.valueOf(expectedUsageAmount))
                        .withSavings(expectedSavings).build();
        expectedModel.setCoupon(couponToBeUsed, expectedCoupon);
        String expectedMessage = String.format(UsedCommand.MESSAGE_USED_COUPON_SUCCESS, expectedCoupon.getName());

        assertCommandSuccess(usedCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredCouponList().size() + 1);
        UsedCommand usedCommand = new UsedCommand(outOfBoundIndex);

        assertCommandFailure(usedCommand, model, Messages.MESSAGE_INVALID_COUPON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showCouponAtIndex(model, TypicalIndexes.INDEX_FIRST_COUPON);

        Coupon couponToBeUsed = model.getFilteredCouponList().get(TypicalIndexes.INDEX_FIRST_COUPON.getZeroBased());
        UsedCommand usedCommand = new UsedCommand(TypicalIndexes.INDEX_FIRST_COUPON);

        ModelManager expectedModel = new ModelManager(model.getCouponStash(), new UserPrefs());
        Integer expectedUsageAmount = Integer.parseInt(couponToBeUsed.getUsage().value) + 1;
        Coupon expectedCoupon =
                new CouponBuilder(couponToBeUsed).withUsage(String.valueOf(expectedUsageAmount)).build();
        expectedModel.setCoupon(couponToBeUsed, expectedCoupon);

        String expectedMessage = String.format(UsedCommand.MESSAGE_USED_COUPON_SUCCESS, expectedCoupon.getName());

        assertCommandSuccess(usedCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexFilteredListWithOriginalAmount_success() {
        showCouponAtIndex(model, TypicalIndexes.INDEX_SECOND_COUPON);

        Coupon couponToBeUsed = model.getFilteredCouponList().get(TypicalIndexes.INDEX_FIRST_COUPON.getZeroBased());
        MonetaryAmount originalAmount = new MonetaryAmount(10.0);
        UsedCommand usedCommand = new UsedCommand(TypicalIndexes.INDEX_FIRST_COUPON, originalAmount);

        ModelManager expectedModel = new ModelManager(model.getCouponStash(), new UserPrefs());
        Integer expectedUsageAmount = Integer.parseInt(couponToBeUsed.getUsage().value) + 1;
        Savings expectedSavings = SavingsConversionUtil.convertToPure(couponToBeUsed.getSavings(), originalAmount);
        Coupon expectedCoupon =
                new CouponBuilder(couponToBeUsed).withUsage(String.valueOf(expectedUsageAmount))
                        .withSavings(expectedSavings).build();
        expectedModel.setCoupon(couponToBeUsed, expectedCoupon);

        String expectedMessage = String.format(UsedCommand.MESSAGE_USED_COUPON_SUCCESS, expectedCoupon.getName());

        assertCommandSuccess(usedCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showCouponAtIndex(model, TypicalIndexes.INDEX_FIRST_COUPON);

        Index outOfBoundIndex = TypicalIndexes.INDEX_SECOND_COUPON;
        // ensures that outOfBoundIndex is still in bounds of CouponStash list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getCouponStash().getCouponList().size());

        UsedCommand usedCommand = new UsedCommand(outOfBoundIndex);

        assertCommandFailure(usedCommand, model, Messages.MESSAGE_INVALID_COUPON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        UsedCommand usedFirstCommand = new UsedCommand(TypicalIndexes.INDEX_FIRST_COUPON);
        UsedCommand usedSecondCommand = new UsedCommand(TypicalIndexes.INDEX_THIRD_COUPON);
        UsedCommand usedThirdCommandWithOriginalAmount =
                new UsedCommand(TypicalIndexes.INDEX_SECOND_COUPON, new MonetaryAmount(10.00));

        // same object -> returns true
        assertTrue(usedFirstCommand.equals(usedFirstCommand));

        // same values -> returns true
        UsedCommand usedFirstCommandCopy = new UsedCommand(TypicalIndexes.INDEX_FIRST_COUPON);
        assertTrue(usedFirstCommand.equals(usedFirstCommandCopy));

        // different types -> returns false
        assertFalse(usedFirstCommand.equals(1));

        // null -> returns false
        assertFalse(usedFirstCommand.equals(null));

        // different command -> returns false
        assertFalse(usedFirstCommand.equals(usedSecondCommand));

        // different original amount -> returns false
        assertFalse(usedFirstCommand.equals(usedThirdCommandWithOriginalAmount));
    }
}
