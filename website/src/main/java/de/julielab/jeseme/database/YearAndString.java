package de.julielab.jeseme.database;

public class YearAndString {
	public Integer year;
	public String value;

	public YearAndString(final Integer year, final String value) {
		this.year = year;
		this.value = value;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final YearAndString other = (YearAndString) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (year == null) {
			if (other.year != null)
				return false;
		} else if (!year.equals(other.year))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((value == null) ? 0 : value.hashCode());
		result = (prime * result) + ((year == null) ? 0 : year.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "YearAndString [year=" + year + ", value=" + value + "]";
	}

}
