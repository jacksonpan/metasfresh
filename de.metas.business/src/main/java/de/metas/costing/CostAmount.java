package de.metas.costing;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.util.Check;

import de.metas.quantity.Quantity;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * de.metas.business
 * %%
 * Copyright (C) 2018 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

@Value
public class CostAmount
{
	public static final CostAmount of(@NonNull final BigDecimal value, final int currencyId)
	{
		return new CostAmount(value, currencyId);
	}

	public static final CostAmount zero(final int currencyId)
	{
		return new CostAmount(BigDecimal.ZERO, currencyId);
	}

	BigDecimal value;
	int currencyId;

	@Builder
	private CostAmount(@NonNull final BigDecimal value, final int currencyId)
	{
		Check.assume(currencyId > 0, "currencyId > 0");
		this.value = value;
		this.currencyId = currencyId;
	}

	private final void assertCurrencyMatching(final CostAmount amt)
	{
		if (currencyId != amt.currencyId)
		{
			throw new AdempiereException("Amount has invalid currency: " + amt + ". Expected: " + currencyId);
		}
	}

	public int signum()
	{
		return value.signum();
	}

	public boolean isZero()
	{
		return signum() == 0;
	}

	public CostAmount negate()
	{
		if (value.signum() == 0)
		{
			return this;
		}

		return new CostAmount(value.negate(), currencyId);
	}

	public CostAmount negateIf(final boolean condition)
	{
		return condition ? negate() : this;
	}

	public CostAmount negateIfNot(final boolean condition)
	{
		return !condition ? negate() : this;
	}

	public CostAmount multiply(@NonNull final BigDecimal multiplicand)
	{
		if (BigDecimal.ONE.compareTo(multiplicand) == 0)
		{
			return this;
		}

		return new CostAmount(value.multiply(multiplicand), currencyId);
	}

	public CostAmount multiply(@NonNull final Quantity quantity)
	{
		return multiply(quantity.getQty());
	}

	public CostAmount add(@NonNull final CostAmount amtToAdd)
	{
		assertCurrencyMatching(amtToAdd);

		if (amtToAdd.isZero())
		{
			return this;
		}
		if (isZero())
		{
			return amtToAdd;
		}

		return new CostAmount(value.add(amtToAdd.value), currencyId);
	}

	public CostAmount divide(final BigDecimal divisor, final int precision, final RoundingMode roundingMode)
	{
		final BigDecimal valueNew = value.divide(divisor, precision, roundingMode);
		return new CostAmount(valueNew, currencyId);
	}
	
	public CostAmount divide(final Quantity divisor, final int precision, final RoundingMode roundingMode)
	{
		return divide(divisor.getQty(), precision, roundingMode);
	}

	public CostAmount roundToPrecisionIfNeeded(final int precision)
	{
		if (value.scale() <= precision)
		{
			return this;
		}

		final BigDecimal valueNew = value.setScale(precision, RoundingMode.HALF_UP);
		return new CostAmount(valueNew, currencyId);
	}

	public CostAmount subtract(@NonNull final CostAmount amtToSubtract)
	{
		assertCurrencyMatching(amtToSubtract);

		if (amtToSubtract.isZero())
		{
			return this;
		}
		return new CostAmount(value.subtract(amtToSubtract.value), currencyId);
	}

	public CostAmount subtract(@NonNull final BigDecimal amtToSubtract)
	{
		if (amtToSubtract.signum() == 0)
		{
			return this;
		}

		return new CostAmount(value.subtract(amtToSubtract), currencyId);
	}
}