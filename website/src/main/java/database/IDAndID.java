package database;

public class IDAndID {
	public Integer WORD1;
	public Integer WORD2;

	public IDAndID(Integer id1, Integer id2) {
		this.WORD1 = id1;
		this.WORD2 = id2;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IDAndID [id1=" + this.WORD1 + ", id2=" + this.WORD2 + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.WORD1 == null) ? 0 : this.WORD1.hashCode());
		result = prime * result
				+ ((this.WORD2 == null) ? 0 : this.WORD2.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IDAndID))
			return false;
		IDAndID other = (IDAndID) obj;
		if (this.WORD1 == null) {
			if (other.WORD1 != null)
				return false;
		} else if (!this.WORD1.equals(other.WORD1))
			return false;
		if (this.WORD2 == null) {
			if (other.WORD2 != null)
				return false;
		} else if (!this.WORD2.equals(other.WORD2))
			return false;
		return true;
	}
	
}
