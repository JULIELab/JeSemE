package database;

public class YearAndAssociation {
	public Integer year;
	public Float association;

	public YearAndAssociation(Integer year, Float association) {
		this.year = year;
		this.association = association;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "YearAndAssociation [year=" + this.year + ", association="
				+ this.association + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.association == null) ? 0 : this.association.hashCode());
		result = prime * result
				+ ((this.year == null) ? 0 : this.year.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof YearAndAssociation))
			return false;
		YearAndAssociation other = (YearAndAssociation) obj;
		if (this.association == null) {
			if (other.association != null)
				return false;
		} else if (!this.association.equals(other.association))
			return false;
		if (this.year == null) {
			if (other.year != null)
				return false;
		} else if (!this.year.equals(other.year))
			return false;
		return true;
	}

}
