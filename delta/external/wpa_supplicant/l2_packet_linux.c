/*
 * WPA Supplicant - Layer2 packet handling with Linux packet sockets
 * Copyright (c) 2003-2005, Jouni Malinen <j@w1.fi>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * Alternatively, this software may be distributed under the terms of BSD
 * license.
 *
 * See README and COPYING for more details.
 */

#include "includes.h"
#include <sys/ioctl.h>
#include <netpacket/packet.h>
#include <net/if.h>

#include "common.h"
#include "eloop.h"
#include "l2_packet.h"
#include "wpa_supplicant_i.h"


struct l2_packet_data {
	int fd; /* packet socket for EAPOL frames */
	char ifname[IFNAMSIZ + 1];
	int ifindex;
	u8 own_addr[ETH_ALEN];
	void (*rx_callback)(void *ctx, const u8 *src_addr,
			    const u8 *buf, size_t len);
	void *rx_callback_ctx;
	int l2_hdr; /* whether to include layer 2 (Ethernet) header data
		     * buffers */
};

int l2_packet_get_own_addr(struct l2_packet_data *l2, u8 *addr)
{
	os_memcpy(addr, l2->own_addr, ETH_ALEN);
	return 0;
}

int l2_packet_send(struct l2_packet_data *l2, const u8 *dst_addr, u16 proto,
		   const u8 *buf, size_t len)
{
#ifdef WAPI
        u8 txbuf[2312]= {0,};
        u8 *pos = txbuf;
#endif
	int ret;
	if (l2 == NULL)
		return -1;
	if (l2->l2_hdr) {
		ret = send(l2->fd, buf, len, 0);
		if (ret < 0)
			perror("l2_packet_send - send");
	} else {
		struct sockaddr_ll ll;
		os_memset(&ll, 0, sizeof(ll));
		ll.sll_family = AF_PACKET;
		ll.sll_ifindex = l2->ifindex;
		ll.sll_protocol = htons(proto);
		ll.sll_halen = ETH_ALEN;
#ifdef WAPI
        if (proto==ETH_P_WAI) {
                memcpy (pos, dst_addr,   ETH_ALEN); pos += ETH_ALEN;
                memcpy (pos, l2->own_addr,   ETH_ALEN);pos += ETH_ALEN;
                memcpy(pos,  &ll.sll_protocol, 2); pos+=2;
                memcpy (pos, buf, len);
                wpa_hexdump(MSG_ERROR, "txbuf", txbuf, len + ETH_HLEN);
                ret = sendto(l2->fd, txbuf, len+ETH_HLEN, 0, (struct sockaddr *) &ll,
                             sizeof(ll));
                wpa_printf(MSG_DEBUG, "l2_packet_send: ret = '%d'", ret);
        }
        else {
#endif
		os_memcpy(ll.sll_addr, dst_addr, ETH_ALEN);
		ret = sendto(l2->fd, buf, len, 0, (struct sockaddr *) &ll,
			     sizeof(ll));
#ifdef WAPI
        }
#endif

		if (ret < 0)
			perror("l2_packet_send - sendto");
	}
	return ret;
}


static void l2_packet_receive(int sock, void *eloop_ctx, void *sock_ctx)
{
	struct l2_packet_data *l2 = eloop_ctx;
	u8 buf[2300];
	int res;
#ifdef WAPI
	struct l2_ethhdr l2_hdr;
	struct wpa_supplicant *wpa_s = (struct wpa_supplicant *)l2->rx_callback_ctx;
	struct l2_packet_data *l2_wapi = wpa_s->l2_wapi;
	int is_wapi = 0;
#endif
	struct sockaddr_ll ll;
	socklen_t fromlen;

#ifdef WAPI
	wpa_printf(MSG_DEBUG, "%s: l2=%p, wpa_s->l2_wapi=%p\n", 
		   __FUNCTION__, l2, l2_wapi);

	if (l2 == l2_wapi) {
		is_wapi = 1;
		memset(&l2_hdr, 0, sizeof(l2_hdr));
		res = recvfrom(sock, buf, sizeof(buf), 0, NULL,NULL);
	}
	else {
#endif
	os_memset(&ll, 0, sizeof(ll));
	fromlen = sizeof(ll);
	res = recvfrom(sock, buf, sizeof(buf), 0, (struct sockaddr *) &ll,
		       &fromlen);
#ifdef WAPI
	}
#endif
	if (res < 0) {
		perror("l2_packet_receive - recvfrom");
		return;
	}

#ifdef WAPI
	if (is_wapi) {
		memcpy(&l2_hdr, buf, ETH_HLEN);
        l2_hdr.h_proto = ntohs (l2_hdr.h_proto);
        res -= ETH_HLEN;
        if (res > 0)
			memmove(buf, (char *)buf+ETH_HLEN, res);
        else
			res = 0;
		l2->rx_callback(l2->rx_callback_ctx, l2_hdr.h_source, buf, res);
	}
	else {
#endif
	l2->rx_callback(l2->rx_callback_ctx, ll.sll_addr, buf, res);
#ifdef WAPI
	}
#endif
}


struct l2_packet_data * l2_packet_init(
	const char *ifname, const u8 *own_addr, unsigned short protocol,
	void (*rx_callback)(void *ctx, const u8 *src_addr,
			    const u8 *buf, size_t len),
	void *rx_callback_ctx, int l2_hdr)
{
	struct l2_packet_data *l2;
	struct ifreq ifr;
	struct sockaddr_ll ll;

	wpa_printf(MSG_DEBUG, "Entering l2_packet_init, protocol = 0x%0x...", protocol);
	l2 = os_zalloc(sizeof(struct l2_packet_data));
	if (l2 == NULL)
		return NULL;
	os_strncpy(l2->ifname, ifname, sizeof(l2->ifname));
	l2->rx_callback = rx_callback;
	l2->rx_callback_ctx = rx_callback_ctx;
	l2->l2_hdr = l2_hdr;

#ifdef WAPI
        if (protocol == ETH_P_WAI)
                l2->fd = socket(PF_PACKET,  SOCK_RAW ,  htons(protocol));
        else
#endif
	l2->fd = socket(PF_PACKET, l2_hdr ? SOCK_RAW : SOCK_DGRAM,
			htons(protocol));
	if (l2->fd < 0) {
		perror("socket(PF_PACKET)");
		os_free(l2);
		return NULL;
	}
	os_strncpy(ifr.ifr_name, l2->ifname, sizeof(ifr.ifr_name));
	if (ioctl(l2->fd, SIOCGIFINDEX, &ifr) < 0) {
		perror("ioctl[SIOCGIFINDEX]");
		close(l2->fd);
		os_free(l2);
		return NULL;
	}
	l2->ifindex = ifr.ifr_ifindex;

	os_memset(&ll, 0, sizeof(ll));
	ll.sll_family = PF_PACKET;
	ll.sll_ifindex = ifr.ifr_ifindex;
	ll.sll_protocol = htons(protocol);
	if (bind(l2->fd, (struct sockaddr *) &ll, sizeof(ll)) < 0) {
		perror("bind[PF_PACKET]");
		close(l2->fd);
		os_free(l2);
		return NULL;
	}

	if (ioctl(l2->fd, SIOCGIFHWADDR, &ifr) < 0) {
		perror("ioctl[SIOCGIFHWADDR]");
		close(l2->fd);
		os_free(l2);
		return NULL;
	}
	os_memcpy(l2->own_addr, ifr.ifr_hwaddr.sa_data, ETH_ALEN);

	eloop_register_read_sock(l2->fd, l2_packet_receive, l2, NULL);
	if (l2==0) {
	    wpa_printf(MSG_ERROR, " l2 == 0\n");
	}

	wpa_printf(MSG_DEBUG, "%s: l2 = %p, l2->fd = %d\n", __FUNCTION__, l2, l2->fd);

	return l2;
}


void l2_packet_deinit(struct l2_packet_data *l2)
{
	if (l2 == NULL)
		return;

	if (l2->fd >= 0) {
		eloop_unregister_read_sock(l2->fd);
		close(l2->fd);
	}
		
	os_free(l2);
}


int l2_packet_get_ip_addr(struct l2_packet_data *l2, char *buf, size_t len)
{
	int s;
	struct ifreq ifr;
	struct sockaddr_in *saddr;

	s = socket(PF_INET, SOCK_DGRAM, 0);
	if (s < 0) {
		perror("socket");
		return -1;
	}
	os_memset(&ifr, 0, sizeof(ifr));
	os_strncpy(ifr.ifr_name, l2->ifname, sizeof(ifr.ifr_name));
	if (ioctl(s, SIOCGIFADDR, &ifr) < 0) {
		if (errno != EADDRNOTAVAIL)
			perror("ioctl[SIOCGIFADDR]");
		close(s);
		return -1;
	}
	close(s);
	saddr = (struct sockaddr_in *) &ifr.ifr_addr;
	if (saddr->sin_family != AF_INET)
		return -1;
	os_snprintf(buf, len, "%s", inet_ntoa(saddr->sin_addr));
	return 0;
}


void l2_packet_notify_auth_start(struct l2_packet_data *l2)
{
}
