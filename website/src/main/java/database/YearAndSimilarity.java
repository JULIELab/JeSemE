package database;

public class YearAndSimilarity {
	public Integer year;
	public Float similarity;

	public YearAndSimilarity(Integer year, Float similarity) {
		this.year = year;
		this.similarity = similarity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "YearAndSimilarity [year=" + this.year + ", similarity="
				+ this.similarity + "]";
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
		result = prime * result
				+ ((this.similarity == null) ? 0 : this.similarity.hashCode());
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
		if (!(obj instanceof YearAndSimilarity))
			return false;
		YearAndSimilarity other = (YearAndSimilarity) obj;
		if (this.similarity == null) {
			if (other.similarity != null)
				return false;
		} else if (!this.similarity.equals(other.similarity))
			return false;
		if (this.year == null) {
			if (other.year != null)
				return false;
		} else if (!this.year.equals(other.year))
			return false;
		return true;
	}

}
