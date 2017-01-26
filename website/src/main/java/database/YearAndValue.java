package database;

public class YearAndValue {
	public Integer year;
	public Float value;

	public YearAndValue(Integer year, Float value) {
		this.year = year;
		this.value = value;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "YearAndValue [year=" + this.year + ", value=" + this.value
				+ "]";
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
				+ ((this.value == null) ? 0 : this.value.hashCode());
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
		if (!(obj instanceof YearAndValue))
			return false;
		YearAndValue other = (YearAndValue) obj;
		if (this.value == null) {
			if (other.value != null)
				return false;
		} else if (!this.value.equals(other.value))
			return false;
		if (this.year == null) {
			if (other.year != null)
				return false;
		} else if (!this.year.equals(other.year))
			return false;
		return true;
	}

}
