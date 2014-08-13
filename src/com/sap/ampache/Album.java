/**
 * 
 */
package com.sap.ampache;

import java.io.Serializable;

/**
 * 
 * @author Chen Xiaoliang
 */
public class Album implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	private String artist;
	private int artistId;
	private int tracks;
	private String art;

	/**
	 * 
	 */
	public Album() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the artist
	 */
	public String getArtist() {
		return artist;
	}

	/**
	 * @param artist the artist to set
	 */
	public void setArtist(String artist) {
		this.artist = artist;
	}

	/**
	 * @return the artistId
	 */
	public int getArtistId() {
		return artistId;
	}

	/**
	 * @param artistId the artistId to set
	 */
	public void setArtistId(int artistId) {
		this.artistId = artistId;
	}

	/**
	 * @return the tracks
	 */
	public int getTracks() {
		return tracks;
	}

	/**
	 * @param tracks the tracks to set
	 */
	public void setTracks(int tracks) {
		this.tracks = tracks;
	}

	/**
	 * @return the art
	 */
	public String getArt() {
		return art;
	}

	/**
	 * @param art the art to set
	 */
	public void setArt(String art) {
		this.art = art;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

}
